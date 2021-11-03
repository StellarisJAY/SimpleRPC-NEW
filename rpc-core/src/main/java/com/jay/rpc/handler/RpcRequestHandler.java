package com.jay.rpc.handler;

import com.jay.rpc.discovery.ServiceMapper;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;


/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final ApplicationContext applicationContext;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public RpcRequestHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, RpcRequest rpcRequest) {
        LOGGER.info("接收到RPC请求，目标接口：{}，目标方法：{}", rpcRequest.getTargetClass(), rpcRequest.getMethodName());
        // 目标类
        Class<?> targetClass = rpcRequest.getTargetClass();


        Object instance = ServiceMapper.getServiceImpl(targetClass);
        LOGGER.info("已获得服务实现类实例：{}", instance);
        // 从Spring容器获取RPC业务Bean
        RpcResponse response = new RpcResponse();
        try{
            // 调用目标方法
            Method targetMethod = targetClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = targetMethod.invoke(instance, rpcRequest.getParameters());

            response.setResult(result);
            response.setReturnType(targetMethod.getReturnType());
        }catch (Exception e){
            LOGGER.error("方法调用异常：", e);
            // 将异常写入响应报文
            response.setError(e);
        }finally {
            // 发送response
            context.channel().writeAndFlush(response);
        }
    }
}
