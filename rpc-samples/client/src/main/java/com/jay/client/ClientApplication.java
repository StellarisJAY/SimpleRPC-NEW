package com.jay.client;

import com.jay.rpc.annotation.EnableRpc;
import com.jay.rpc.annotation.registry.EnableRedisRegistry;
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
@EnableRpc(basePackage = "com.jay.client")
@EnableRedisRegistry
@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
