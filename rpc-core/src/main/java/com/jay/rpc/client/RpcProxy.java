package com.jay.rpc.client;

import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *    RPC 代理工具
 *    生成目标接口的代理对象，代理对象的方法中通过发送RPC请求获取结果
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
@Component
public class RpcProxy {

    /**
     * 代理对象池，避免重复创建同一个接口的代理对象
     */
    private final HashMap<Class<?>, Object> proxyInstances = new HashMap<>(256);

    @Resource
    private RpcClient rpcClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clazz, String serviceName){
        /*
            每一个代理对象都是懒加载单例
            使用单例是为了避免重复创建代理对象
         */
        if(!proxyInstances.containsKey(clazz)){
            synchronized (clazz){
                if(!proxyInstances.containsKey(clazz)){
                    proxyInstances.put(clazz, createInstance(clazz, serviceName));
                }
            }
        }
        return (T)proxyInstances.get(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz, String applicationName){
        /*
            动态代理
            对调用的方法生成代理，代理方法中通过发送RPC请求来获取返回值
         */
        Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            RpcRequest request = RpcRequest.builder()
                    .methodName(method.getName())
                    .parameters(args)
                    .targetClass(clazz)
                    .parameterTypes(method.getParameterTypes())
                    .requestId(UUID.randomUUID().toString())
                    .build();
            CompletableFuture<RpcResponse> future = rpcClient.send(request, applicationName);
            RpcResponse response = future.get();
            if(response.getError() != null){
                throw response.getError();
            }
            return response.getResult();
        });
        // 返回接口类型的RPC实例
        return (T)proxyInstance;
    }
}
