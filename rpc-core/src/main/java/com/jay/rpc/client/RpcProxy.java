package com.jay.rpc.client;

import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 *    RPC 代理工具
 *    获取目标接口的代理对象，代理对象的方法中通过发送RPC请求获取结果
 *    将代理对象单例化，避免重复创建同一个接口的代理对象造成的开销
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
@Component
@Slf4j
public class RpcProxy {

    /**
     * 代理对象池，避免重复创建同一个接口的代理对象
     */
    private final HashMap<Class<?>, Object> proxyInstances = new HashMap<>(256);

    @Resource
    private RpcClient rpcClient;
    @Resource
    private UnfinishedRequestHolder unfinishedRequestHolder;

    /**
     * 默认超时时间：10s
     */
    private static final long DEFAULT_TIMEOUT = 10;

    /**
     * 无超时参数create
     * @param clazz clazz
     * @param serviceName name
     * @param <T> Type
     * @return T
     */
    public <T> T create(Class<T> clazz, String serviceName){
        return create(clazz, serviceName, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 带超时参数create
     * @param clazz clazz
     * @param serviceName name
     * @param timeout timeout
     * @param timeUnit timeUnit
     * @param <T> Type
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clazz, String serviceName, long timeout, TimeUnit timeUnit){
        if(timeout < 0){
            throw new IllegalArgumentException("timeout must be positive");
        }
        /*
            每一个代理对象都是懒加载单例
            使用单例是为了避免重复创建代理对象
            重复创建的代理对象会产生大量垃圾，占用堆内存
         */
        if(!proxyInstances.containsKey(clazz)){
            // 锁代理对象的类对象，避免不同类型的创建过程竞争锁
            synchronized (clazz){
                if(!proxyInstances.containsKey(clazz)){
                    proxyInstances.put(clazz, createInstance(clazz, serviceName, timeout, timeUnit));
                }
            }
        }
        return (T)proxyInstances.get(clazz);
    }

    /**
     * 具有超时时间的实例
     * @param clazz 接口
     * @param applicationName 服务名
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @param <T> 接口TypeParameter
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz, String applicationName, long timeout, TimeUnit timeUnit){
        /*
            动态代理
            对调用的方法生成代理，代理方法中通过发送RPC请求来获取返回值
         */
        Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            // 封装RPC请求
            RpcRequest request = RpcRequest.builder()
                    .methodName(method.getName())
                    .parameters(args)
                    .targetClass(clazz)
                    .parameterTypes(method.getParameterTypes())
                    // 请求UUID
                    .requestId(UUID.randomUUID().toString())
                    .build();
            // 发送RPC请求，得到CompletableFuture
            CompletableFuture<RpcResponse> future = rpcClient.send(request, applicationName);
            try{
                // 等待response，默认超时时间10s
                RpcResponse response = future.get(timeout <= 0 ? DEFAULT_TIMEOUT : timeout, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
                if(response.getError() != null){
                    throw response.getError();
                }
                return response.getResult();
            }catch (TimeoutException e){
                // 超时，删除未完成请求缓存
                unfinishedRequestHolder.remove(request.getRequestId());
                throw new TimeoutException("request timeout");
            }
        });
        // 返回接口类型的RPC实例
        return (T)proxyInstance;
    }
}
