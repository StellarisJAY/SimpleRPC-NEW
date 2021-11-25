package com.jay.rpc.entity;

import lombok.*;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/18
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RpcMessage {
    /**
     * 消息类型
     * @see com.jay.rpc.constants.RpcConstants
     */
    private byte messageType;
    /**
     * 序列化类型
     * @see com.jay.rpc.constants.RpcConstants
     */
    private byte serializer;
    /**
     * 压缩方式
     * @see com.jay.rpc.constants.RpcConstants
     */
    private byte compress;

    /**
     * 请求ID，自增id
     * @see com.jay.rpc.client.RpcClient
     */
    private int requestId;
    /**
     * 数据部分
     */
    private Object data;
}
