package com.jay.rpc.transport.serialize.protostuff;

import com.alibaba.fastjson.JSON;
import com.jay.rpc.transport.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.spi.LocaleServiceProvider;

/**
 * <p>
 *  序列化工具
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class ProtoStuffSerializer implements Serializer {

    private static Map<Class<?>, Schema<?>> schemaMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T object){
        Class<T> clazz = (Class<T>)object.getClass();
        Schema<T> schema = getSchema(clazz);
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            return ProtostuffIOUtil.toByteArray(object, schema, buffer);
        }finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz){
        Schema<T> schema = getSchema(clazz);
        T result = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, result ,schema);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> Schema<T> getSchema(Class<T> clazz){
        Schema<T> schema = (Schema<T>)schemaMap.get(clazz);
        if(schema == null){
            schema = RuntimeSchema.getSchema(clazz);
            if(schema != null) {
                schemaMap.put(clazz, schema);
            }
        }
        return schema;
    }

    public static String serializeJSON(Object object){
        return JSON.toJSONString(object);
    }

    public static <T> T deserializeJSON(String jsonString, Class<T> clazz){
        return JSON.parseObject(jsonString, clazz);
    }
}
