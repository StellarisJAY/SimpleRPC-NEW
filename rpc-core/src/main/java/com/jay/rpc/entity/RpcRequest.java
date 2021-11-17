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
    private String requestId;
    private Class<?> targetClass;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
