package com.jay.rpc.handler.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.handler.Filter;
import com.jay.rpc.handler.filter.exception.FilteredException;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 流量控制过滤器
 * </p>
 *
 * @author Jay
 * @date 2021/11/8
 **/
public class TrafficControlFilter extends Filter {
    private Logger logger = LoggerFactory.getLogger(TrafficControlFilter.class);
    /**
     * RateLimiter 限流器
     */
    private static RateLimiter rateLimiter = RateLimiter.create(1);

    public TrafficControlFilter() {
        super("default-traffic-control-filter");
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
