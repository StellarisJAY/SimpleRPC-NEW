package com.jay.rpc.transport.serialize;

import com.jay.common.extention.SPI;

/**
 * Serializer interface
 * @author Jay
 * @date 2021/11/17
 */
@SPI
public interface Serializer {

    /**
     * 序列化
     * @param object 对象
     * @param <T> 类型
     * @return byte数组
     */
    <T> byte[] serialize(T object);

    /**
     * 反序列化
     * @param bytes byte数组
     * @param clazz 类型
     * @param <T> 类型
     * @return object
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
