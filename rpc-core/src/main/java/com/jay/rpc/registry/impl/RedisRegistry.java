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
    public InetSocketAddress getServiceAddress(String serviceName) {
        String address = redisUtil.get(KEY_ADDRESS_PREFIX + serviceName);
        String ip = address.substring(0, address.indexOf(":"));
        int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
        return new InetSocketAddress(ip, port);
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
        String rootKey = KEY_SERVICE_PREFIX + applicationName;
        String addressKey = KEY_ADDRESS_PREFIX + applicationName;

        /*
            Redis 每条指令是原子的，但是多条指令不是
            多线程下可能导致多个服务注册到一个名字下
         */

        String addrValue = redisUtil.get(addressKey);
        if(!StringUtils.isEmpty(addrValue) && !addrValue.equals(address)){
            throw new RuntimeException("服务名已被注册");
        }
        // 生成注册信息
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        // 序列化
        String serializedInfo = ProtoStuffSerializer.serializeJSON(applicationInfo);
        // 保存信息
        redisUtil.set(rootKey, serializedInfo);
        // 注册服务
        redisUtil.setEx(addressKey, address, heartBeatTime, TimeUnit.SECONDS);

    }

    @Override
    public void heartBeat(String applicationName, String address){
        String rootKey = KEY_SERVICE_PREFIX + applicationName;
        String addressKey = KEY_ADDRESS_PREFIX + applicationName;
        LOGGER.info("Redis心跳续约，appName: {}, addr: {}, 续约时长：{}", applicationName, address, heartBeatTime);
        // 生成注册信息
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        // 序列化
        String serializedInfo = ProtoStuffSerializer.serializeJSON(applicationInfo);
        LOGGER.info("JSON：{}", serializedInfo);
        // 更新信息
        redisUtil.set(rootKey, serializedInfo);
        // 续约一个心跳周期
        redisUtil.setEx(addressKey, address, heartBeatTime, TimeUnit.SECONDS);
    }
}
