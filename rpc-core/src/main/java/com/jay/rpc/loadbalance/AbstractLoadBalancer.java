package com.jay.rpc.loadbalance;


import java.net.InetSocketAddress;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/22
 **/
public abstract class AbstractLoadBalancer implements LoadBalancer {
    @Override
    public InetSocketAddress selectAddress(List<InetSocketAddress> addresses, String applicationName, String requestId) {
        if(addresses == null || addresses.isEmpty()){
            throw new NullPointerException();
        }
        // 候选服务器只有一个，直接返回该服务器
        if(addresses.size() == 1){
            return addresses.get(0);
        }
        return doSelect(addresses, applicationName, requestId);
    }

    public abstract InetSocketAddress doSelect(List<InetSocketAddress> addresses, String applicationName, String requestId);
}
