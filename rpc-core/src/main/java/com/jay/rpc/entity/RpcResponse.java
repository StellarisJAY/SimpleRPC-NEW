package com.jay.rpc.entity;

/**
 * <p>
 *  RPC 返回值
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcResponse {
    private String requestId;
    private Throwable error;
    private Class<?> returnType;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", error=" + error +
                ", returnType=" + returnType +
                ", result=" + result +
                '}';
    }
}
