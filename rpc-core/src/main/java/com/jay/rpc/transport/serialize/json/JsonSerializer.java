package com.jay.rpc.transport.serialize.json;

import com.alibaba.fastjson.JSON;
import com.jay.rpc.transport.serialize.Serializer;

/**
 * <p>
 *  JSON序列化工具
 * </p>
 *
 * @author Jay
 * @date 2021/11/23
 **/
public class JsonSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object) {
        return JSON.toJSONString(object).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }
}
