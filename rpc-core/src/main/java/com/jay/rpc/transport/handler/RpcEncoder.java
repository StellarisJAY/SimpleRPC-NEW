package com.jay.rpc.transport.handler;

import com.jay.common.enums.CompressorTypeEnum;
import com.jay.common.enums.SerializerTypeEnum;
import com.jay.common.extention.ExtensionLoader;
import com.jay.rpc.compress.Compressor;
import com.jay.rpc.constants.RpcConstants;
import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.transport.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *   Rpc消息编码器
 *   消息格式：
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+---------+----- --+-----+-----+-------+
 *   |   magic   num         |version | full length         | messageType| serial|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         data                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 *   magicNumber：魔数, "srpc"
 *   version：版本
 *   fullLength：报文总长度 = 头部16字节 + body，最大Integer.MAX_VALUE
 *   messageType：消息类型，request/response/心跳
 *   serializer：序列化类型，protostuff/kyro
 *   compress：body是否压缩
 *   requestId：消息ID，自增int
 *   data：消息体
 * </p>
 * @see io.netty.handler.codec.MessageToByteEncoder 输入ByteBuf，输出RpcMessage
 * @author Jay
 * @date 2021/10/13
 **/
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out){
        try{
            // 写入魔数和版本
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 暂时跳过长度字段
            out.writerIndex(out.writerIndex() + 4);
            // 写入其他头部信息
            out.writeByte(rpcMessage.getMessageType());
            out.writeByte(rpcMessage.getSerializer());
            out.writeByte(rpcMessage.getCompress());
            out.writeInt(rpcMessage.getRequestId());

            // 总长度，默认为首部长度
            int fullLength = RpcConstants.HEAD_LENGTH;
            byte[] bytes = null;
            // 消息类型是请求或返回
            if(rpcMessage.getMessageType() == RpcConstants.TYPE_RESPONSE || rpcMessage.getMessageType() == RpcConstants.TYPE_REQUEST){
                // 获取序列化工具
                String serializerType = SerializerTypeEnum.getType(rpcMessage.getSerializer());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerType);
                // 序列化
                bytes = serializer.serialize(rpcMessage.getData());
            }

            // 是否需要压缩数据部分
            if(rpcMessage.getCompress() != RpcConstants.COMPRESS_OFF){
                int originalLen = bytes == null ? 0 : bytes.length;
                // 获取压缩类型
                String compressorType = CompressorTypeEnum.getType(rpcMessage.getCompress());
                // SPI获取压缩工具实例
                Compressor compressor = ExtensionLoader.getExtensionLoader(Compressor.class).getExtension(compressorType);
                // 压缩数据部分
                bytes = compressor.compress(bytes);
                log.info("压缩类型：{}，压缩前大小：{}，压缩后大小：{}", compressorType, originalLen, bytes.length);
            }

            // 计算总长度
            fullLength += (bytes == null ? 0 : bytes.length);
            // 写入数据部分
            out.writeBytes(bytes);
            int endIndex = out.writerIndex();
            // writerIndex移动到length字段位置
            out.writerIndex(endIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            // 写入fullLength
            out.writeInt(fullLength);
            out.writerIndex(endIndex);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
