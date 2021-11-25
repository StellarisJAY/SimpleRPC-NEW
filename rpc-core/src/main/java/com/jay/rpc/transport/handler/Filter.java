package com.jay.rpc.transport.handler;

import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.transport.handler.filter.exception.FilteredException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

/**
 * <p>
 *  过滤器抽象类
 *  实现doFilter方法来实现过滤器操作
 *
 * ChannelHandler.Sharable，用户自定义过滤器在系统中是单例，netty的单例handler必须加上该注解
 * @see io.netty.channel.SimpleChannelInboundHandler
 * </p>
 *
 * @author Jay
 * @date 2021/11/8
 **/
@ChannelHandler.Sharable
public abstract class Filter extends SimpleChannelInboundHandler<RpcMessage> {

    protected String filterName;

    public Filter(String filterName) {
        this.filterName = filterName;
    }

    /**
     * netty的channelRead方法，Filter实现该方法提供模板，用户实现doFilter方法提供过滤逻辑
     * @param channelHandlerContext 上下文
     * @param message RpcMessage，RPC报文
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage message){
        try{
            // 执行doFilter，返回true表示放行
            if(doFilter(channelHandlerContext, message)){
                // 传递到下一个Handler
                channelHandlerContext.fireChannelRead(message);
            }
            else{
                // 不放行，response封装Filter异常
                RpcResponse response = new RpcResponse();
                response.setError(new FilteredException(filterName));
                channelHandlerContext.channel().writeAndFlush(response);
            }
        }catch (Exception e){
            // 过滤器抛出异常，将异常封装到response中
            RpcResponse response = new RpcResponse();
            response.setError(e);
            channelHandlerContext.channel().writeAndFlush(response);
        }
    }

    /**
     * 过滤器方法
     * @param context 上下文
     * @param message RPC报文
     * @return boolean 是否放行
     * @throws FilteredException 过滤器抛出异常
     */
    public abstract boolean doFilter(ChannelHandlerContext context, RpcMessage message) throws FilteredException;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filter filter = (Filter) o;
        return filterName.equals(filter.filterName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterName);
    }
}
