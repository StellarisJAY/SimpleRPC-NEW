package com.jay.rpc.loadbalance.impl;

import com.jay.rpc.loadbalance.AbstractLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * <p>
 *  一致性Hash算法
 * </p>
 *
 * @author Jay
 * @date 2021/11/22
 **/
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {
    @Override
    public InetSocketAddress doSelect(List<InetSocketAddress> addresses) {
        return null;
    }
}
