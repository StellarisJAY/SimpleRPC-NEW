package com.jay.rpc.client;

import com.jay.rpc.registry.IRegistry;
import com.jay.rpc.registry.impl.ZooKeeperRegistry;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Proxy;

/**
 * <p>
 *    RPC 代理工具
 *    生成目标接口的代理对象，代理对象的方法中通过发送RPC请求获取结果
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcProxy {

    @Resource
    private IRegistry serviceRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clazz, String serviceName){
        /*
            动态代理
            对调用的方法生成代理，代理方法中通过发送RPC请求来获取返回值
         */
        Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            String address = serviceRegistry.getServiceAddress(serviceName);
            if(StringUtil.isNullOrEmpty(address)){
                throw new RuntimeException("无法找到服务实现");
            }
            int split = address.indexOf(":");
            int port = Integer.parseInt(address.substring(split + 1));

            RpcClient client = new RpcClient(address.substring(0, split), port);
            // 创建RPC请求
            RpcRequest request = new RpcRequest();
            // 服务接口
            request.setTargetClass(clazz);
            // 方法信息
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);
            LOGGER.info("发送RPC请求中，请求报文：{}", request);
            // 发送RPC请求，并同步等待response
            RpcResponse response = client.send(request);
            LOGGER.info("接收到RPC回复，返回：{}", response);
            // response中包含异常，将异常抛出
            if(response.getError() != null){
                throw response.getError();
            }
            return response.getResult();
        });
        // 返回接口类型的RPC实例
        return (T)proxyInstance;
    }
}
