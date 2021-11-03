package com.jay.rpc.config;

import com.jay.rpc.registry.impl.ZooKeeperRegistry;
import com.jay.rpc.util.ZookeeperUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import java.io.IOException;


/**
 * <p>
 *    服务注册中心自动配置类
 *    根据用户在配置文件中指定的服务注册中心类型创建相应的注册中心Bean
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
public class RpcAutoConfiguration {


    @Bean(name="serviceRegistry")
    @Conditional(ZooKeeperCondition.class)
    public ZooKeeperRegistry registry(@Value("${rpc.service.registry.zk.hosts}") String zkHosts,
                                      @Value("${rpc.service.registry.zk.session-timeout}") int sessionTimeout) throws IOException, InterruptedException {
        ZookeeperUtil zookeeperUtil = new ZookeeperUtil(zkHosts, sessionTimeout);
        return new ZooKeeperRegistry(zookeeperUtil);
    }
}
