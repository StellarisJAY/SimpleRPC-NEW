package com.jay.rpc.entity;

import lombok.*;

/**
 * <p>
 *  RPC 返回值
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class RpcResponse {
    private String requestId;
    private Throwable error;
    private Class<?> returnType;
    private Object result;
}
