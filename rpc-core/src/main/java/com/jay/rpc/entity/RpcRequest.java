package com.jay.rpc.entity;


/**
 * <p>
 *  RPC请求
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcRequest {
    /**
     * 服务接口
     */
    private Class<?> targetClass;

    /**
     * 方法信息
     */
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
