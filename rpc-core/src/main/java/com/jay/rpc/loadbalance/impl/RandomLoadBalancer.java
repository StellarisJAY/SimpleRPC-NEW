package com.jay.rpc.loadbalance.impl;

import com.jay.rpc.loadbalance.AbstractLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * <p>
 *  随机负载均衡
 * </p>
 *
 * @author Jay
 * @date 2021/11/22
 **/
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    public InetSocketAddress doSelect(List<InetSocketAddress> addresses) {
        Random random = new Random();
        return addresses.get(random.nextInt(addresses.size()));
    }
}
