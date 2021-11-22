package com.jay.rpc.loadbalance.impl;

import com.jay.rpc.loadbalance.AbstractLoadBalancer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  一致性Hash算法
 * </p>
 *
 * @author Jay
 * @date 2021/11/22
 **/
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

    private static ConcurrentHashMap<String, ConsistentHashSelector> selectorHolder = new ConcurrentHashMap<>();

    @Override
    public InetSocketAddress doSelect(List<InetSocketAddress> addresses, String applicationName, String requestId) {
        long identityHash = System.identityHashCode(applicationName);
        // 获取该服务的选择器
        ConsistentHashSelector selector = selectorHolder.get(applicationName);
        // 选择器不存在，新建选择器
        if(selector == null){
            selector = new ConsistentHashSelector(addresses, identityHash);
            selectorHolder.put(applicationName, selector);
        }
        return selector.select(requestId);
    }


    static class ConsistentHashSelector{
        private long identityHash;
        private TreeMap<Long, InetSocketAddress> hashCircle;

        public ConsistentHashSelector(List<InetSocketAddress> addresses, long identityHash){
            this.identityHash = identityHash;
            this.hashCircle = new TreeMap<>();
            // 把所有地址写入hash环
            for(InetSocketAddress address : addresses){
                long hashcode = hash(address.toString());
                System.out.println(hashcode);
                hashCircle.put(hashcode, address);
            }
        }


        /**
         * 使用一致性hash算法，通过requestId来分配服务器
         * @param requestId requestId
         * @return InetSocketAddress
         */
        public InetSocketAddress select(String requestId){
            long hashcode = hash(requestId);

            // 找到离该hashcode最近的下一个entry
            Map.Entry<Long, InetSocketAddress> firstEntry = hashCircle.tailMap(hashcode, true).firstEntry();
            // 不存在，表示下一个应该是头节点
            if(firstEntry == null){
                firstEntry = hashCircle.firstEntry();
            }
            return firstEntry.getValue();
        }

        private long hash(String str){
            return str.hashCode();
        }
    }
}
