package com.jay.admin;

import com.jay.rpc.annotation.EnableRpc;
import com.jay.rpc.annotation.registry.EnableRedisRegistry;
import com.jay.rpc.annotation.registry.EnableZooKeeperRegistry;
import com.jay.rpc.util.RedisLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/4
 **/
@EnableRpc(basePackage = "com.jay.admin")
@EnableRedisRegistry
@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
