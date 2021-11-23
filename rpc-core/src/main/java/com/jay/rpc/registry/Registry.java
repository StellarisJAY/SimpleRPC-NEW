package com.jay.rpc.registry;

import com.jay.rpc.discovery.ServiceMapper;
import com.jay.rpc.entity.ServerInfo;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务注册抽象类
 * 实现该接口的方法来创建一种服务注册方式
 * @author Jay
 */
public abstract class Registry {

    protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "heart-beat"));
    protected int heartBeatTime = 30;
    /**
     * 获取服务地址
     * @param serviceName 服务名称
     * @return String
     */
    public abstract List<InetSocketAddress> getServiceAddress(String serviceName);

    /**
     * 服务发现
     * @return List
     */
    public abstract Map<String, List<ServerInfo>> discoverService();

    /**
     * 注册服务
     * @param applicationName 服务名
     * @param address 地址
     * @throws Exception Exception
     */
    public abstract void registerService(String applicationName, String address) throws Exception;

    /**
     * 心跳
     * @param applicationName 服务名
     * @param address 地址
     */
    public abstract void heartBeat(String applicationName, String address);

    /**
     * 开启心跳
     * 模板方法，实现heartBeat()来开启心跳检测
     * 通过定时任务线程池来完成心跳
     * @param applicationName 服务名
     * @param address 地址
     */
    public void startHearBeat(String applicationName, String address){
        // 延迟一个周期开启
        executor.scheduleAtFixedRate(()->heartBeat(applicationName, address), heartBeatTime, heartBeatTime, TimeUnit.SECONDS);
    }

    public ServerInfo getApplicationInfo(String applicationName, String address){
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setApplicationName(applicationName);
        serverInfo.setAddress(address);
        serverInfo.setLastHeartBeatTime(System.currentTimeMillis());
        serverInfo.setServiceCount(ServiceMapper.getServiceCount());
        serverInfo.setServiceInterfaces(ServiceMapper.getServiceInterfaces());

        return serverInfo;
    }
}
