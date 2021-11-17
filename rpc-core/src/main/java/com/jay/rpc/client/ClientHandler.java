package com.jay.rpc.client;

import com.jay.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <p>
 *  客户端处理器，处理收到的response
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 未完成请求缓存
     */
    private final UnfinishedRequestHolder unfinishedRequestHolder;

    public ClientHandler(UnfinishedRequestHolder unfinishedRequestHolder) {
        this.unfinishedRequestHolder = unfinishedRequestHolder;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        unfinishedRequestHolder.complete(rpcResponse);
    }
}
