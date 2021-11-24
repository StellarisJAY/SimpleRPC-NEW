package com.jay.sample.server;

import com.jay.rpc.annotation.EnableRpc;
import com.jay.rpc.annotation.registry.EnableRedisRegistry;
import com.jay.rpc.annotation.registry.EnableZooKeeperRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
@EnableRpc(basePackage = "com.jay.sample.server")
@EnableRedisRegistry
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
