package com.jay.rpc.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/4
 **/
public class RedisCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String type = context.getEnvironment().getProperty("rpc.service.registry.type");
        return "redis".equalsIgnoreCase(type);
    }
}
