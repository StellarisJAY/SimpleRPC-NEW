package com.jay.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     在实现类上添加该注解，声明该类为Rpc服务实现类。
 *     使用RpcService后不会影响Bean被Spring容器管理
 *     使用后将Bean实例记录在ServiceMapper中
 * </p>
 * @author Jay
 * @date 2021/10/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RpcService {
}
