package com.jay.rpc.transport.handler;

import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.transport.handler.filter.exception.FilteredException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <p>
 *  过滤器抽象类
 *  实现doFilter方法来实现过滤器操作
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

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage message) throws Exception {
        try{
            if(doFilter(channelHandlerContext, message)){
                channelHandlerContext.fireChannelRead(message);
            }
            else{
                RpcResponse response = new RpcResponse();
                response.setError(new FilteredException(filterName));
                channelHandlerContext.channel().writeAndFlush(response);
            }
        }catch (Exception e){
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
}
