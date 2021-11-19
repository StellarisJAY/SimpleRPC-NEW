package com.jay.rpc.client;

import com.jay.rpc.constants.RpcConstants;
import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  客户端处理器，处理收到的response
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    /**
     * 未完成请求缓存
     */
    private final UnfinishedRequestHolder unfinishedRequestHolder;

    public ClientHandler(UnfinishedRequestHolder unfinishedRequestHolder) {
        this.unfinishedRequestHolder = unfinishedRequestHolder;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage message)  {
        log.info("收到response：消息ID：{}", message.getRequestId());
        // 消息类型为Response
        if(message.getMessageType() == RpcConstants.TYPE_RESPONSE){
            RpcResponse response = (RpcResponse)message.getData();
            log.info("收到response：id={}", response.getRequestId());
            // 完成未完成请求
            unfinishedRequestHolder.complete(response);
        }
    }
}
