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
 *    生成目标接口的代理对象，代理对象的方法中通过发送RPC请求获取结果
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

    private static final long DEFAULT_TIMEOUT = 10;
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
    private <T> T createInstance(Class<T> clazz, String applicationName, long timeout, TimeUnit timeUnit){
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
            try{
                // 等待response，默认超时时间10s
                RpcResponse response = future.get(timeout == 0 ? DEFAULT_TIMEOUT : timeout, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
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

    /**
     * 无超时时间参数
     * @param clazz clazz
     * @param applicationName applicationName
     * @param <T> type
     * @return proxyInstance
     */
    private <T> T createInstance(Class<T> clazz, String applicationName){
        return createInstance(clazz, applicationName, 0, null);
    }
}
