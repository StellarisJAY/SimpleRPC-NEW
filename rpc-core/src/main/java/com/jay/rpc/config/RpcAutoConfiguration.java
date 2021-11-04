package com.jay.rpc.config;

import com.jay.rpc.config.condition.RedisCondition;
import com.jay.rpc.config.condition.ZooKeeperCondition;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.registry.impl.RedisRegistry;
import com.jay.rpc.registry.impl.ZooKeeperRegistry;
import com.jay.rpc.util.RedisUtil;
import com.jay.rpc.util.ZookeeperUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;


/**
 * <p>
 *    服务注册中心自动配置类
 *    根据用户在配置文件中指定的服务注册中心类型创建相应的注册中心Bean
 *    rpc.service.registry.type=zookeeper,开启ZooKeeper服务注册中心
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
public class RpcAutoConfiguration {

    /**
     * 创建 ZooKeeper注册中心Bean
     * Conditional注解会判断是否需要创建该Bean
     * @param zkHosts zk地址
     * @param sessionTimeout 连接超时时间
     * @return ZooKeeperRegistry
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    @Bean
    @Conditional(ZooKeeperCondition.class)
    public Registry zkRegistry(@Value("${rpc.service.registry.zk.hosts}") String zkHosts,
                               @Value("${rpc.service.registry.zk.session-timeout}") int sessionTimeout) throws IOException, InterruptedException {
        // 创建ZooKeeper实例，创建过程中建立连接
        ZookeeperUtil zookeeperUtil = new ZookeeperUtil(zkHosts, sessionTimeout);
        // 创建注册中心Bean
        return new ZooKeeperRegistry(zookeeperUtil);
    }

    @Bean
    @Conditional(RedisCondition.class)
    public Registry redisRegistry(@Value("${rpc.service.registry.redis.host}") String host,
                                  @Value("${rpc.service.registry.redis.port}") int port,
                                  @Value("${rpc.service.registry.redis.password}") String password,
                                  @Value("${rpc.service.registry.redis.timeout}") int timeout){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, host, port);
        RedisUtil redisUtil = new RedisUtil(jedisPool);
        return new RedisRegistry(redisUtil);
    }
}
