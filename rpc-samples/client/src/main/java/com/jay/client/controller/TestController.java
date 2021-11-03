package com.jay.client.controller;

import com.jay.rpc.client.RpcProxy;
import com.jay.sample.api.UserService;
import com.jay.sample.api.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
@RestController
public class TestController {
    @Resource
    private RpcProxy rpcProxy;

    @GetMapping("/test")
    public UserDTO test(@RequestParam("name") String name){
        UserService userService = rpcProxy.create(UserService.class, "server-application");
        return userService.getUser(name);
    }
}
