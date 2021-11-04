package com.jay.rpc.annotation;

import com.jay.rpc.RpcServer;
import com.jay.rpc.client.RpcProxy;
import com.jay.rpc.config.RpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC启动注解
 * 将自动导入RPC服务器、Zookeeper服务注册等Bean
 * 该注解将导入RpcServer来启动一个RPC服务器
 * 导入RpcAutoConfiguration完成了其他Bean的配置
 * RpcProxy提供了客户端所需的动态代理
 * @author Jay
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RpcServer.class, RpcAutoConfiguration.class, RpcProxy.class})
public @interface EnableRpc {
}
