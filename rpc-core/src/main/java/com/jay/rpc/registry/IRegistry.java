package com.jay.rpc.registry;

import java.util.List;

/**
 * @author Jay
 */
public interface IRegistry {
    /**
     * 获取服务地址
     * @param serviceName 服务名称
     * @return String
     */
    String getServiceAddress(String serviceName);

    /**
     * 服务发现
     * @return List
     */
    List<String> discoverService();

    /**
     * 注册服务
     * @param applicationName 服务名
     * @param address 地址
     */
    void registerService(String applicationName, String address) throws Exception;
}
