package com.jay.rpc.annotation.registry;

import com.jay.rpc.registry.impl.RedisRegistry;
import com.jay.rpc.util.RedisUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用该注解来开启Redis注册中心
 * @author Jay
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RedisUtil.class, RedisRegistry.class})
public @interface EnableRedisRegistry {
}
