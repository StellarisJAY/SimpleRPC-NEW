package com.jay.rpc.handler;

import com.jay.rpc.entity.RpcHeader;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object out, ByteBuf byteBuf) throws Exception {
        if(out != null){
            // 写入协议头魔数 2字节
            byteBuf.writeShort(RpcHeader.MAGIC);
            // 写入协议头 消息类型 1字节
            if(out instanceof RpcResponse){
                byteBuf.writeByte(RpcHeader.TYPE_RESPONSE);
                //状态位，1字节
                byteBuf.writeByte(RpcHeader.STATUS_RECV);
            }
            else if(out instanceof RpcRequest){
                byteBuf.writeByte(RpcHeader.TYPE_REQUEST);
                //状态位，1字节
                byteBuf.writeByte(RpcHeader.STATUS_SEND);
            }
            else{
                // 心跳包
                byteBuf.writeByte(RpcHeader.TYPE_HEARTBEAT);
                //状态位，1字节
                byteBuf.writeByte(RpcHeader.STATUS_SEND);
            }
            // 消息id，8字节
            byteBuf.writeLong(RpcHeader.nextId());

            // 序列化
            byte[] serialized = SerializationUtil.serialize(out);
            // 协议头写入消息体长度 4 字节
            byteBuf.writeInt(serialized.length);
            // 写入消息体
            byteBuf.writeBytes(serialized);
        }

    }
}
