package com.jay.rpc.annotation.registry;

import com.jay.rpc.registry.impl.ZooKeeperRegistry;
import com.jay.rpc.util.ZookeeperUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用该注解来开启ZooKeeper注册中心
 * @author Jay
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ZookeeperUtil.class, ZooKeeperRegistry.class})
public @interface EnableZooKeeperRegistry {
}
