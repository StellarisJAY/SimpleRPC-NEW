package com.jay.rpc.discovery;

import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * <p>
 *     服务接口-实现类实例 映射
 *     因为无法直接通过接口获取到实现类的Bean
 *     所以在启动时通过扫描带有@RpcService注解的Bean作为实现类，并记录在该Map中
 * </p>
 *
 * @author Jay
 * @date 2021/10/29
 **/
public class ServiceMapper {
    /**
     * 记录 接口和实现类的映射
     * HashMap初始大小设为256来避免扩容
     */
    private static final HashMap<Class<?>, Object> map = new HashMap<>(256);

    /**
     * 获取服务接口的实现Bean
     * @param service 接口
     * @param bean bean
     */
    public static void put(Class<?> service, Object bean){
        if(map.containsKey(service)){
            throw new RuntimeException("RPC接口重复实现，接口：" + service);
        }
        map.put(service, bean);
    }

    /**
     * 获取服务实现类
     * @param service 服务接口
     * @return bean
     */
    public static Object getServiceImpl(Class<?> service){
        return map.get(service);
    }
}
