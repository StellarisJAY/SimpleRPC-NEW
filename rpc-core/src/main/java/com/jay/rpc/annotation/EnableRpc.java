package com.jay.rpc.annotation;

import com.jay.rpc.config.CustomScannerRegistry;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC启动注解
 * basePackage来指定RPC服务提供类的包
 * @author Jay
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({CustomScannerRegistry.class})
public @interface EnableRpc {
    String basePackage() default "";
}
