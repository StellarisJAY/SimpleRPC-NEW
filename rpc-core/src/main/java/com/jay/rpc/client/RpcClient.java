package com.jay.rpc.client;

import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.transport.handler.RpcDecoder;
import com.jay.rpc.transport.handler.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 *   RPC客户端
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
@Component
@Slf4j
public class RpcClient {
    private final NioEventLoopGroup group = new NioEventLoopGroup(4);
    private final Bootstrap bootstrap;

    private final Registry registry;
    private final UnfinishedRequestHolder unfinishedRequestHolder;
    private final ChannelProvider channelProvider;

    @Autowired
    public RpcClient(Registry registry, UnfinishedRequestHolder unfinishedRequestHolder, ChannelProvider channelProvider){
        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel){
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // Rpc编码器，将Rpc请求序列化
                        pipeline.addLast(new RpcEncoder());
                        // Rpc解码器，将Rpc返回反序列化
                        pipeline.addLast(new RpcDecoder());
                        pipeline.addLast(new ClientHandler(unfinishedRequestHolder));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, false);
        this.registry = registry;
        this.unfinishedRequestHolder = unfinishedRequestHolder;
        this.channelProvider = channelProvider;
    }

    /**
     * 发送rpc请求
     * @param request 请求
     * @return response
     * @throws Exception 抛出异常，由调用者捕获
     */
    public CompletableFuture<RpcResponse> send(RpcRequest request, String applicationName) throws Exception{
        // 从注册中心找到address
        InetSocketAddress address = registry.getServiceAddress(applicationName);
        // 获取channel
        Channel channel = getChannel(address);
        CompletableFuture<RpcResponse> result = new CompletableFuture<>();
        // 发送请求，使用listener监听发送状态
        channel.writeAndFlush(request).addListener((ChannelFutureListener)future->{
           if(future.isSuccess()){
               log.info("请求发送成功，requestId：{}", request.getRequestId());
               // 请求发送成功
               unfinishedRequestHolder.put(request.getRequestId(), result);
           }
           else{
               result.completeExceptionally(new RuntimeException("failed to send request"));
           }
        });
        return result;
    }

    /**
     * 建立连接
     * @param address 地址
     * @return Channel
     * @throws ExecutionException e
     * @throws InterruptedException e
     */
    private Channel doConnect(InetSocketAddress address) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> result = new CompletableFuture<>();
        bootstrap.connect(address).addListener((ChannelFutureListener)future->{
            if(future.isSuccess()){
                result.complete(future.channel());
            }
            else{
                throw new RuntimeException("无法建立连接");
            }
        });
        return result.get();
    }

    /**
     * 获取复用channel
     * @param address 地址
     * @return Channel
     * @throws ExecutionException e
     * @throws InterruptedException e
     */
    private Channel getChannel(InetSocketAddress address) throws ExecutionException, InterruptedException {
        if(address == null) {
            throw new NullPointerException();
        }
        Channel channel = channelProvider.get(address.toString());
        if(channel == null){
            channel = doConnect(address);
            channelProvider.put(address.toString(), channel);
        }
        return channel;
    }
}
