package com.jay.rpc.loadbalance.impl;

import com.jay.rpc.loadbalance.AbstractLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * <p>
 *  随机负载均衡
 *  随机从服务器列表选择一台
 * </p>
 *
 * @author Jay
 * @date 2021/11/22
 **/
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    public InetSocketAddress doSelect(List<InetSocketAddress> addresses, String applicationName, String requestId) {
        Random random = new Random();
        // 从addresses列表随机选择一个地址
        return addresses.get(random.nextInt(addresses.size()));
    }
}
