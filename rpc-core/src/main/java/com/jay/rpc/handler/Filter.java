package com.jay.rpc.handler;

import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.handler.filter.exception.FilteredException;
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
public abstract class Filter extends SimpleChannelInboundHandler<RpcRequest> {

    protected String filterName;

    public Filter(String filterName) {
        this.filterName = filterName;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
        try{
            if(doFilter(channelHandlerContext, request)){
                channelHandlerContext.fireChannelRead(request);
            }
            else{
                RpcResponse response = new RpcResponse();
                response.setError(new FilteredException(filterName));
            }
        }catch (Exception e){
            RpcResponse response = new RpcResponse();
            response.setError(e);
            channelHandlerContext.writeAndFlush(response);
        }
    }

    /**
     * 过滤器方法
     * @param context 上下文
     * @param request RPC请求
     * @return boolean 是否放行
     */
    public abstract boolean doFilter(ChannelHandlerContext context, RpcRequest request) throws FilteredException;
}
