package com.jay.rpc.client;

import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.handler.RpcDecoder;
import com.jay.rpc.handler.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CountDownLatch;

/**
 * <p>
 *   RPC客户端
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private final NioEventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap;
    private final String host;
    private final int port;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private RpcResponse response = null;
    private void init(){
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel){
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // Rpc编码器，将Rpc请求序列化
                        pipeline.addLast(new RpcEncoder());
                        // Rpc解码器，将Rpc返回反序列化
                        pipeline.addLast(new RpcDecoder());
                        pipeline.addLast(RpcClient.this);
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    public RpcClient(String host, int port){
        init();
        this.host = host;
        this.port = port;
    }

    /**
     * 发送rpc请求
     * @param request 请求
     * @return response
     * @throws Exception 抛出异常，由调用者捕获
     */
    public RpcResponse send(RpcRequest request) throws Exception{
        try{
            // 客户端建立连接
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // 连接成功
            if(channelFuture.isSuccess()){

                // 发送rpcRequest
                channelFuture.channel().writeAndFlush(request).sync();
                countDownLatch.await();
            }
            // 关闭channel
            channelFuture.channel().closeFuture();
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 接收到response
     * @param channelHandlerContext 上下文
     * @param rpcResponse response
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse){
        this.response = rpcResponse;
        // 通知send
        countDownLatch.countDown();
    }
}
