package com.jay.admin.controller;

import com.jay.rpc.entity.ServerInfo;
import com.jay.rpc.registry.Registry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务中心api
 * </p>
 *
 * @author Jay
 * @date 2021/11/4
 **/
@RestController
public class ServiceRegistryController {
    @Resource
    private Registry registry;

    @GetMapping("/info")
    public List<InetSocketAddress> getServiceAddress(@RequestParam("service") String serviceName){
        return registry.getServiceAddress(serviceName);
    }

    @GetMapping("/discovery")
    public Map<String, List<ServerInfo>> discoverServices(){
        return registry.discoverService();
    }
}
