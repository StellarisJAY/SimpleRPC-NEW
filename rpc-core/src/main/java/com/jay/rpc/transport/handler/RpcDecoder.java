package com.jay.rpc.transport.handler;

import com.jay.common.enums.CompressorTypeEnum;
import com.jay.common.enums.SerializerTypeEnum;
import com.jay.common.extention.ExtensionLoader;
import com.jay.rpc.compress.Compressor;
import com.jay.rpc.constants.RpcConstants;
import com.jay.rpc.entity.RpcMessage;
import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.entity.RpcResponse;
import com.jay.rpc.transport.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 *      Rpc消息解码器
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
 * @see io.netty.handler.codec.LengthFieldBasedFrameDecoder 解决粘包拆包
 * </p>
 *
 * @author Jay
 * @date 2021/11/18
 **/
@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder() {
        /*
            maxFrameLength：报文最大长度
            lengthFieldOffset：长度字段偏移，即长度字段在报文中的位置，此处为第六个字节，下标为5
            lengthAdjustment：长度字段结束位置+长度值 - lengthAdjustment = decode返回数据结尾位置
            initialBytesToStrip：decode方法返回的数据的起始位置，此处因为要校验魔数和版本，所以起始位置是0
         */
        this(RpcConstants.MAX_MESSAGE_LENGTH, 5, 4, -9, 0);
    }

    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);

    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // LengthBasedDecoder的decode
        Object temp = super.decode(ctx, in);
        if(temp != null){
            ByteBuf frame = (ByteBuf)temp;
            // 报文小于最大长度
            if(frame.readableBytes() >= RpcConstants.MIN_TOTAL_LENGTH){
                try{
                    // 解析
                    return decode(frame);
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                    throw e;
                }finally {
                    frame.release();
                }

            }
        }
        return temp;
    }

    private RpcMessage decode(ByteBuf frame){
        // 检查魔数和版本
        checkMagicNumber(frame);
        checkVersion(frame);

        // 读取头部信息
        int fullLength = frame.readInt();
        byte messageType = frame.readByte();
        byte serializerCode = frame.readByte();
        byte compress = frame.readByte();
        int requestId = frame.readInt();

        // 封装头部信息
        RpcMessage rpcMessage = RpcMessage.builder().messageType(messageType)
                .compress(compress)
                .serializer(serializerCode)
                .requestId(requestId)
                .build();
        // 数据部分长度
        int dataLength = fullLength - RpcConstants.HEAD_LENGTH;
        if(dataLength > 0){
            // 读取数据部分
            byte[] bytes = new byte[dataLength];
            frame.readBytes(bytes);
            // 解压数据部分
            if(compress != RpcConstants.COMPRESS_OFF){
                // 找到压缩器类型
                String compressorType = CompressorTypeEnum.getType(compress);
                // SPI获取压缩器实例
                Compressor compressor = ExtensionLoader.getExtensionLoader(Compressor.class).getExtension(compressorType);
                // 解压
                bytes = compressor.decompress(bytes);
            }
            // 获取序列化工具名称
            String serializerType = SerializerTypeEnum.getType(rpcMessage.getSerializer());
            // SPI 工具加载序列化类
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerType);
            Object data = null;
            // 根据message类型，反序列化
            switch(rpcMessage.getMessageType()){
                case RpcConstants.TYPE_REQUEST : data = serializer.deserialize(bytes, RpcRequest.class);break;
                case RpcConstants.TYPE_RESPONSE : data = serializer.deserialize(bytes, RpcResponse.class);break;
                default:
            }
            rpcMessage.setData(data);
        }
        return rpcMessage;
    }

    private void checkMagicNumber(ByteBuf frame){
        byte[] buffer = new byte[RpcConstants.MAGIC_NUMBER.length];
        frame.readBytes(buffer);
        for(int i = 0; i < buffer.length; i++){
            if(buffer[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new RuntimeException("Unknown magic number");
            }
        }
    }

    private void checkVersion(ByteBuf frame){
        byte version = frame.readByte();
        if(version != RpcConstants.VERSION){
            throw new RuntimeException("incompatible version");
        }
    }
}
