package com.jay.rpc.registry.impl;

import com.jay.rpc.entity.ApplicationInfo;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.util.RedisUtil;
import com.jay.rpc.transport.serialize.protostuff.ProtoStuffSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    /**
     * 服务地址 key 前缀
     */
    private final String KEY_ADDRESS_PREFIX = "rpc.address.";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * 该服务器的分布式锁UUID
     */
    private static final String LOCK_UUID = UUID.randomUUID().toString();

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
    public List<ApplicationInfo> discoverService() {
        // 获取所有的服务key
        Set<String> keys = redisUtil.keys(KEY_SERVICE_PREFIX + "*");

        return keys.stream().map(key -> {
            String serializedInfo = redisUtil.get(key);
            LOGGER.info("JSON信息：{}", serializedInfo);
            ApplicationInfo info = ProtoStuffSerializer.deserializeJSON(serializedInfo, ApplicationInfo.class);

            // 获取服务地址，获取到null表示服务已下线
            String addrKey = KEY_ADDRESS_PREFIX + info.getApplicationName();
            String addr = redisUtil.get(addrKey);
            info.setAddress(addr);
            // 设置服务状态
            info.setAlive(addr != null);
            return info;
        }).collect(Collectors.toList());
    }

    @Override
    public void registerService(String applicationName, String address) throws Exception {
        String addressKey = KEY_SERVICE_PREFIX + applicationName + "." + address;
        // 生成注册信息
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        // 序列化
        String serializedInfo = ProtoStuffSerializer.serializeJSON(applicationInfo);
        // 注册服务，key=地址，value=主机信息
        redisUtil.setEx(addressKey, serializedInfo, heartBeatTime, TimeUnit.SECONDS);

    }

    @Override
    public void heartBeat(String applicationName, String address){
        String addressKey = KEY_SERVICE_PREFIX + applicationName + "." + address;
        LOGGER.info("Redis心跳续约，appName: {}, addr: {}, 续约时长：{}", applicationName, address, heartBeatTime);
        // 生成注册信息
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        // 序列化
        String serializedInfo = ProtoStuffSerializer.serializeJSON(applicationInfo);
        LOGGER.info("JSON：{}", serializedInfo);
        // 续约一个心跳周期
        redisUtil.setEx(addressKey, serializedInfo, heartBeatTime, TimeUnit.SECONDS);
    }
}
