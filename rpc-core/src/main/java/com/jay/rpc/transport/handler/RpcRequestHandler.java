package com.jay.rpc.transport.handler;

import com.jay.rpc.constants.RpcConstants;
import com.jay.rpc.discovery.ServiceMapper;
import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;


/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext context, RpcMessage message) {
        RpcMessage.RpcMessageBuilder respondMessageBuilder = RpcMessage.builder()
                .serializer(message.getSerializer())
                .requestId(message.getRequestId())
                .compress(message.getCompress());
        byte messageType = message.getMessageType();
        // 接收到请求
        if(RpcConstants.TYPE_REQUEST == messageType){
            // 处理请求
            RpcResponse response = handleRequest(context, (RpcRequest) message.getData());
            respondMessageBuilder.messageType(RpcConstants.TYPE_RESPONSE)
                    .data(response);
        }
        // 收到心跳请求
        else if(RpcConstants.TYPE_HEARTBEAT_REQUEST == messageType){
            respondMessageBuilder.messageType(RpcConstants.TYPE_HEARTBEAT_RESPONSE)
                    .data(null);
        }
        log.info("请求处理完成：id={}", message.getRequestId());
        context.channel().writeAndFlush(respondMessageBuilder.build());
    }

    private RpcResponse handleRequest(ChannelHandlerContext context, RpcRequest rpcRequest){
        log.info("接收到RPC请求，来自：{}, 目标接口：{}，目标方法：{}",context.channel().remoteAddress(), rpcRequest.getTargetClass(), rpcRequest.getMethodName());
        // 目标类
        Class<?> targetClass = rpcRequest.getTargetClass();

        // 获取接口实现类
        Object instance = ServiceMapper.getServiceImpl(targetClass);
        log.info("已获得服务实现类实例：{}", instance);
        // 从Spring容器获取RPC业务Bean
        RpcResponse.RpcResponseBuilder responseBuilder = RpcResponse
                .builder()
                .requestId(rpcRequest.getRequestId());
        try{
            // 调用目标方法
            Method targetMethod = targetClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = targetMethod.invoke(instance, rpcRequest.getParameters());
            // 方法返回类型，如果是void，将返回类型设为Object，避免序列化错误
            responseBuilder
                    .result(result)
                    .returnType(targetMethod.getReturnType() == Void.TYPE ? Object.class : targetMethod.getReturnType());
        }catch (Exception e){
            log.error("方法调用异常：", e);
            // 将异常写入响应报文
            responseBuilder.error(e);
        }
        return responseBuilder.build();
    }
}
