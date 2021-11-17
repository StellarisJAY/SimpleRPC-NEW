package com.jay.rpc.client;

import com.jay.rpc.entity.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  未完成请求缓存
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
@Component
public class UnfinishedRequestHolder {
    /**
     * 请求id-请求completableFuture
     */
    private Map<String, CompletableFuture<RpcResponse>> futureMap = new ConcurrentHashMap<>();

    /**
     * put
     * @param requestId requestId
     * @param future future
     */
    public void put(String requestId, CompletableFuture<RpcResponse> future){
        futureMap.put(requestId, future);
    }

    /**
     * complete
     * @param requestId requestId
     * @param response response
     */
    public void complete(String requestId, RpcResponse response){
        CompletableFuture<RpcResponse> future = futureMap.remove(requestId);
        future.complete(response);
    }
}
