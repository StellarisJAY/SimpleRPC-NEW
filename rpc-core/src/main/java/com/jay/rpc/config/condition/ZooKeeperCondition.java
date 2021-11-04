package com.jay.rpc.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * <p>
 *    判断是否需要满足创建ZooKeeper相关Bean的条件
 *    即，rpc.service.registry.type是否等于zookeeper
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
public class ZooKeeperCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String type = context.getEnvironment().getProperty("rpc.service.registry.type");
        return "Zookeeper".equalsIgnoreCase(type);
    }
}
