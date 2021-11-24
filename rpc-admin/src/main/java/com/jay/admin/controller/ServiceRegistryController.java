package com.jay.admin.controller;

import com.jay.rpc.entity.ServiceInfo;
import com.jay.rpc.registry.Registry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务中心api
 * </p>
 *
 * @author Jay
 * @date 2021/11/4
 **/
@Controller
public class ServiceRegistryController {
    @Resource
    private Registry registry;

    @RequestMapping("/services")
    public String discoverServices(Model model){
        List<ServiceInfo> services = registry.discoverService();
        model.addAttribute("serviceList", services);
        return "services";
    }

    @RequestMapping("/serviceDetail")
    public String serviceDetail(){
        return "serviceDetail";
    }
}
