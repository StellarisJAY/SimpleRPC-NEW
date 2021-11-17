package com.jay.rpc.transport.handler.filter.exception;

/**
 * <p>
 * 过滤器拦截异常
 * </p>
 *
 * @author Jay
 * @date 2021/11/8
 **/
public class FilteredException extends Exception {
    public FilteredException(String message) {
        super(message);
    }
}
