package com.jay.rpc.entity;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Rpc协议头
 * 长度16字节：
 * ---******-----**********-----************-----********-----************---
 * |  0xBABE  |  type 1Byte  |  status 1Byte  |  id 8Byte  |  length 4Byte  |
 * ---******-----**********-----************-----********-----************---
 * 魔数：0xBABE
 * 类型：请求/返回/心跳
 * 状态：发送/接收
 * id：8字节，自增
 * 消息体长度：4字节
 * </p>
 *
 * @author Jay
 * @date 2021/10/18
 **/
public class RpcHeader {
    /**
     * 2字节，魔数BABE
     */
    public static final short MAGIC = (short)0xBABE;

    /**
     * 自增id provider
     * 从MIN_VALUE开始的8字节Long，id用完几乎不可能
     * 使用原子Long避免线程安全问题
     */
    private static final AtomicLong ID_PROVIDER = new AtomicLong(Long.MIN_VALUE);

    /**
     * 类型-请求
     */
    public static final byte TYPE_REQUEST = 0;
    /**
     * 类型-回复
     */
    public static final byte TYPE_RESPONSE = 1;
    /**
     * 类型-心跳
     * 心跳包用于测试consumer和服务provider的连接状态
     */
    public static final byte TYPE_HEARTBEAT = 2;

    public static final byte STATUS_SEND = 0;
    public static final byte STATUS_RECV = 1;

    /**
     * 首部长度
     */
    public static final int HEADER_SIZE = 16;

    /**
     * 获取下一个自增id
     * @return long
     */
    public static long nextId(){
        return ID_PROVIDER.getAndIncrement();
    }

}
