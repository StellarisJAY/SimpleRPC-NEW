package com.jay.rpc.entity;


import lombok.*;

/**
 * <p>
 *  RPC请求
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RpcRequest {
    /**
     * 请求ID，UUID
     * @see com.jay.rpc.client.RpcProxy
     */
    private String requestId;
    /**
     * 目标接口
     */
    private Class<?> targetClass;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型列表
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数
     */
    private Object[] parameters;
}
