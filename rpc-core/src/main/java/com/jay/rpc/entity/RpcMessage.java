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
    private byte messageType;
    private byte serializer;
    private byte compress;

    private int requestId;
    private Object data;
}
