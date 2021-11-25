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
    /**
     * 对应请求ID
     */
    private String requestId;
    /**
     * 方法抛出异常
     */
    private Throwable error;
    /**
     * 返回值类型
     */
    private Class<?> returnType;
    /**
     * 返回值
     */
    private Object result;
}
