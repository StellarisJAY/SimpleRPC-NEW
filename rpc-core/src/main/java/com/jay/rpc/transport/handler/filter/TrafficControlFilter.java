package com.jay.rpc.transport.handler.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.transport.handler.Filter;
import com.jay.rpc.transport.handler.filter.exception.FilteredException;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>
 * 流量控制过滤器
 * </p>
 *
 * @author Jay
 * @date 2021/11/8
 **/
public class TrafficControlFilter extends Filter {
    /**
     * RateLimiter 限流器
     */
    private static volatile RateLimiter rateLimiter;

    public TrafficControlFilter(int rate) {
        super("default-traffic-control-filter");
        createRateLimiter(rate);
    }

    /**
     * DCL 创建RateLimiter懒加载单例
     * @param rate rate
     */
    private void createRateLimiter(int rate){
        if(rateLimiter == null){
            synchronized (TrafficControlFilter.class){
                if(rateLimiter == null){
                    rateLimiter = RateLimiter.create(rate);
                }
            }
        }
    }

    @Override
    public boolean doFilter(ChannelHandlerContext context, RpcRequest request) throws FilteredException {
        if(!rateLimiter.tryAcquire()){
            throw new FilteredException("Blocked by Traffic Control");
        }
        return true;
    }

    public static void setRate(int permitsPerSecond){
        rateLimiter.setRate(permitsPerSecond);
    }
}
