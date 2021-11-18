package com.jay.common.extention;

/**
 *
 * @author Jay
 * @date 2021/11/18
 **/
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
