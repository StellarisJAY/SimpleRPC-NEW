package com.jay.rpc.registry.impl;

import com.jay.rpc.registry.ApplicationInfo;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.util.RedisUtil;
import com.jay.rpc.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
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

    private final RedisUtil redisUtil;
    private final String KEY_SERVICE_PREFIX = "rpc.service.";
    private final String KEY_ADDRESS_PREFIX = "rpc.address.";
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public RedisRegistry(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }


    @Override
    public String getServiceAddress(String serviceName) {
        return redisUtil.get(KEY_ADDRESS_PREFIX + serviceName);
    }

    @Override
    public List<ApplicationInfo> discoverService() {
        // 获取所有的服务key
        Set<String> keys = redisUtil.keys(KEY_SERVICE_PREFIX + "*");
        List<ApplicationInfo> applicationInfos = keys.stream().map(key -> {
            String serializedInfo = redisUtil.get(key);
            LOGGER.info("JSON信息：{}", serializedInfo);
            ApplicationInfo info = SerializationUtil.deserializeJSON(serializedInfo, ApplicationInfo.class);

            String addrKey = KEY_ADDRESS_PREFIX + info.getApplicationName();
            String addr = redisUtil.get(addrKey);
            if(addr != null){
                info.setAddress(addr);
            }
            info.setAlive(addr != null);
            return info;
        }).collect(Collectors.toList());

        return applicationInfos;
    }

    @Override
    public void registerService(String applicationName, String address) throws Exception {
        String rootKey = KEY_SERVICE_PREFIX + applicationName;
        String addressKey = KEY_ADDRESS_PREFIX + applicationName;

        // key已被占用，且地址不同
        String addrValue = redisUtil.get(addressKey);
        if(!StringUtils.isEmpty(addrValue) && !addrValue.equals(address)){
            throw new RuntimeException("服务名已被注册");
        }
        // 生成注册信息
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        // 序列化
        String serializedInfo = SerializationUtil.serializeJSON(applicationInfo);
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
        String serializedInfo = SerializationUtil.serializeJSON(applicationInfo);
        LOGGER.info("JSON：{}", serializedInfo);
        // 更新信息
        redisUtil.set(rootKey, serializedInfo);
        // 续约一个心跳周期
        redisUtil.setEx(addressKey, address, heartBeatTime, TimeUnit.SECONDS);
    }
}
