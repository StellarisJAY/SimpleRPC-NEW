package com.jay.rpc.registry.impl;

import com.jay.rpc.registry.Registry;
import com.jay.rpc.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 使用Redis作为服务注册中心
 * 通过Redis的超时key来服务注册
 * 通过每隔一段时间向Redis重置超时时间的方式心跳检测
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
public class RedisRegistry extends Registry {

    private final RedisUtil redisUtil;
    private final String KEY_PREFIX = "rpc.service.";
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public RedisRegistry(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }


    @Override
    public String getServiceAddress(String serviceName) {
        return redisUtil.get(KEY_PREFIX + serviceName + ".address");
    }

    @Override
    public List<String> discoverService() {
        return null;
    }

    @Override
    public void registerService(String applicationName, String address) throws Exception {
        String rootKey = KEY_PREFIX + applicationName;
        String addressKey = rootKey + ".address";
        String addrValue = redisUtil.get(addressKey);
        if(!StringUtils.isEmpty(addrValue)){
            throw new RuntimeException("服务名已被注册");
        }

        redisUtil.set(rootKey, "info");
        redisUtil.setEx(addressKey, address, heartBeatTime, TimeUnit.SECONDS);
    }

    @Override
    public void heartBeat(String applicationName, String address){
        String rootKey = KEY_PREFIX + applicationName;
        String addressKey = rootKey + ".address";
        LOGGER.info("Redis心跳续约，appName: {}, addr: {}, 续约时长：{}", applicationName, address, heartBeatTime);

        // 续约一个心跳周期
        redisUtil.setEx(addressKey, address, heartBeatTime, TimeUnit.SECONDS);
    }
}
