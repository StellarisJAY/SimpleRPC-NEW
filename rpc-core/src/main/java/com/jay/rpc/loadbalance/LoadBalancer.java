package com.jay.rpc.loadbalance;

import com.jay.common.extention.SPI;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡
 * @author Jay
 * @date 2021/11/22
 */
@SPI
public interface LoadBalancer {
    /**
     * 选择地址
     * @param addresses 地址列表
     * @return InetSocketAddress
     */
    InetSocketAddress selectAddress(List<InetSocketAddress> addresses);
}
