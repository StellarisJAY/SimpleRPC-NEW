package com.jay.rpc.registry.impl;

import com.jay.common.extention.ExtensionLoader;
import com.jay.rpc.entity.ServerInfo;
import com.jay.rpc.entity.ServiceInfo;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.transport.serialize.Serializer;
import com.jay.rpc.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 使用Redis作为服务注册中心
 * 通过Redis的超时key来注册
 * 通过每隔一段时间向Redis重置超时时间的方式心跳检测
 *
 * key格式：
 * rpc.service.{服务名}.{地址} = 主机信息
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
public class RedisRegistry extends Registry {
    @Resource
    private final RedisUtil redisUtil;
    /**
     * 服务信息 key 前缀
     */
    private final String KEY_SERVICE_PREFIX = "rpc.service.";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public RedisRegistry(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }


    @Override
    public List<InetSocketAddress> getServiceAddress(String serviceName) {
        Set<String> addresses = redisUtil.keys(KEY_SERVICE_PREFIX + serviceName + ".*");
        return addresses.stream().map(key -> {
            String prefix = KEY_SERVICE_PREFIX + serviceName + ".";
            String address = key.substring(prefix.length());
            System.out.println(address);
            String ip = address.substring(0, address.indexOf(":"));
            int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
            return new InetSocketAddress(ip, port);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ServiceInfo> discoverService() {
        // 获取所有的服务key
        Set<String> keys = redisUtil.keys(KEY_SERVICE_PREFIX + "*");
        // 解析keys
        Map<String, List<ServerInfo>> resultMap = new HashMap<>(16);
        keys.forEach(key -> {
            // key is like rpc.service.xxx@192.168.154.128:9000
            int split = key.lastIndexOf('@');
            String applicationName = key.substring(KEY_SERVICE_PREFIX.length(), split);

            List<ServerInfo> servers = resultMap.computeIfAbsent(applicationName, k -> new ArrayList<>());

            String serializedInfo = redisUtil.get(key);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("json");
            ServerInfo info = serializer.deserialize(serializedInfo.getBytes(), ServerInfo.class);
            // 上次心跳时间间隔超过了心跳周期
            info.setAlive(System.currentTimeMillis() - info.getLastHeartBeatTime() > heartBeatTime);
            servers.add(info);
        });

        List<ServiceInfo> result = new ArrayList<>(resultMap.keySet().size());
        for (String applicationName : resultMap.keySet()) {
            ServiceInfo serviceInfo = ServiceInfo.builder()
                    .serviceName(applicationName)
                    .servers(resultMap.get(applicationName))
                    .serverCount(resultMap.get(applicationName).size())
                    .build();
            result.add(serviceInfo);
        }
        return result;
    }

    @Override
    public void registerService(String applicationName, String address) {
        String addressKey = KEY_SERVICE_PREFIX + applicationName + "@" + address;
        // 生成注册信息
        ServerInfo serverInfo = getServerInfo(address);
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("json");
        // 序列化
        String serializedInfo = new String(serializer.serialize(serverInfo));
        // 注册服务，key=地址，value=主机信息
        redisUtil.setEx(addressKey, serializedInfo, heartBeatTime, TimeUnit.SECONDS);

    }

    @Override
    public void heartBeat(String applicationName, String address){
        String addressKey = KEY_SERVICE_PREFIX + applicationName + "@" + address;
        LOGGER.info("Redis心跳续约，appName: {}, addr: {}, 续约时长：{}", applicationName, address, heartBeatTime);
        // 生成注册信息
        ServerInfo serverInfo = getServerInfo(address);
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("json");
        // 序列化
        String serializedInfo = new String(serializer.serialize(serverInfo));
        // 续约一个心跳周期
        redisUtil.setEx(addressKey, serializedInfo, heartBeatTime, TimeUnit.SECONDS);
    }
}
