package com.jay.rpc.handler;

import com.jay.rpc.entity.RpcHeader;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * <p>
 *      Rpc消息解码器
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 报文大小是否有16字节
        if(byteBuf.readableBytes() < RpcHeader.HEADER_SIZE){
            return ;
        }
        byteBuf.markReaderIndex();
        short magicNumber = byteBuf.readShort();
        // 读取到魔数
        if(magicNumber == RpcHeader.MAGIC){
            // 读取消息头其他部分
            int type = byteBuf.readByte();
            // 状态和id
            int status = byteBuf.readByte();
            long id = byteBuf.readLong();
            // 读取消息体长度
            int length = byteBuf.readInt();

            // 剩余部分大小为length
            if(byteBuf.readableBytes() == length){
                // 读取消息体
                byte[] buffer = new byte[length];
                byteBuf.readBytes(buffer);

                // 反序列化消息体
                switch(type){
                    case RpcHeader.TYPE_REQUEST : list.add(SerializationUtil.deserialize(buffer, RpcRequest.class)); break;
                    case RpcHeader.TYPE_RESPONSE : list.add(SerializationUtil.deserialize(buffer, RpcResponse.class)); break;
                    default: break;
                }
            }
        }
    }
}
