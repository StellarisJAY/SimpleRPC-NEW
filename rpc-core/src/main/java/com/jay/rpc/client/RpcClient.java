package com.jay.rpc.client;

import com.jay.common.enums.SerializerTypeEnum;
import com.jay.common.extention.ExtensionLoader;
import com.jay.rpc.constants.RpcConstants;
import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.loadbalance.LoadBalancer;
import com.jay.rpc.registry.Registry;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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
    /**
     * 注册中心
     */
    @Resource
    private Registry registry;
    /**
     * 未完成请求缓存
     */
    @Resource
    private UnfinishedRequestHolder unfinishedRequestHolder;
    /**
     * 请求id提供机构
     */
    private final AtomicInteger idProvider = new AtomicInteger(0);
    /**
     * 连接池
     */
    @Resource
    private ChannelProvider channelProvider;

    /**
     * 发送rpc请求
     * @param request 请求
     * @return response
     * @throws Exception 抛出异常，由调用者捕获
     */
    public CompletableFuture<RpcResponse> send(RpcRequest request, String applicationName) throws Exception{
        // 从注册中心找到address
        List<InetSocketAddress> addresses = registry.getServiceAddress(applicationName);
        // 获取负载均衡器
        LoadBalancer loadBalancer = ExtensionLoader.getExtensionLoader(LoadBalancer.class).getExtension("random");
        // 负载均衡器选择地址
        InetSocketAddress address = loadBalancer.selectAddress(addresses, applicationName, request.getRequestId());
        // 获取channel
        Channel channel = getChannel(address);
        // 封装RpcMessage
        RpcMessage message = RpcMessage.builder().data(request)
                // 消息类型
                .messageType(RpcConstants.TYPE_REQUEST)
                // 压缩方式
                .compress((byte) 1)
                // 请求ID
                .requestId(idProvider.getAndIncrement())
                // 序列化方式
                .serializer(SerializerTypeEnum.PROTOSTUFF.code)
                .build();

        CompletableFuture<RpcResponse> result = new CompletableFuture<>();
        // 发送请求，使用listener监听发送状态
        channel.writeAndFlush(message).addListener((ChannelFutureListener)future->{
            // 请求发送成功
           if(future.isSuccess()){
               log.info("请求发送成功，消息Id：{}，requestId：{}", message.getRequestId(), request.getRequestId());
               // 加入到未完成请求缓存
               unfinishedRequestHolder.put(request.getRequestId(), new UnfinishedRequestHolder.UnfinishedRequest(result, channel, address));
           }
           else{
               // 请求发送失败，future异常完成
               result.completeExceptionally(new RuntimeException("failed to send request"));
               // 释放连接池的channel
               channelProvider.get(address).release(channel);
           }
        });
        return result;
    }

    /**
     * 获取复用channel
     * @param address 地址
     * @return Channel
     * @throws ExecutionException e
     * @throws InterruptedException e
     */
    private Channel getChannel(InetSocketAddress address) throws ExecutionException, InterruptedException {
        Future<Channel> future = channelProvider.get(address).acquire();
        return future.get();
    }
}
