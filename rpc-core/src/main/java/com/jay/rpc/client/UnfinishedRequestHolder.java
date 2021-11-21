package com.jay.rpc.client;

import com.jay.rpc.entity.RpcResponse;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  未完成请求缓存
 *  缓存请求的CompletableFuture、channel、address
 *  需要channel和 address来释放连接池的连接
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
@Component
public class UnfinishedRequestHolder {
    @Resource
    private ChannelProvider channelProvider;
    /**
     * 请求id-请求completableFuture
     */
    private final Map<String, UnfinishedRequest> futureMap = new ConcurrentHashMap<>();

    /**
     * put
     * @param requestId requestId
     * @param unfinishedRequest unfinishedRequest
     */
    public void put(String requestId, UnfinishedRequest unfinishedRequest){
        futureMap.put(requestId, unfinishedRequest);
    }

    /**
     * complete
     * @param response response
     */
    public void complete(RpcResponse response){
        assert response.getRequestId() == null;
        UnfinishedRequest unfinishedRequest = futureMap.remove(response.getRequestId());
        // complete future
        unfinishedRequest.future.complete(response);
        // 连接池释放连接
        channelProvider.get(unfinishedRequest.address).release(unfinishedRequest.channel);
    }

    public void remove(String requestId){
        futureMap.remove(requestId);
    }


    static class UnfinishedRequest{
        CompletableFuture<RpcResponse> future;
        Channel channel;
        InetSocketAddress address;

        public UnfinishedRequest(CompletableFuture<RpcResponse> future, Channel channel, InetSocketAddress address) {
            this.future = future;
            this.channel = channel;
            this.address = address;
        }
    }
}
