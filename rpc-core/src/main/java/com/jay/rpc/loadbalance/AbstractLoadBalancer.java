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
    public InetSocketAddress selectAddress(List<InetSocketAddress> addresses) {
        if(addresses == null || addresses.isEmpty()){
            throw new NullPointerException();
        }
        if(addresses.size() == 1){
            return addresses.get(0);
        }
        return doSelect(addresses);
    }

    public abstract InetSocketAddress doSelect(List<InetSocketAddress> addresses);
}
