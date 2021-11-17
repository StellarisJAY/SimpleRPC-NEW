package com.jay.rpc.entity;

import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2021/11/5
 **/
public class ApplicationInfo {
    private String applicationName;
    private String address;
    private long lastHeartBeatTime;
    private int serviceCount;
    private Set<Class<?>> serviceInterfaces;

    private boolean alive;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(long lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(int serviceCount) {
        this.serviceCount = serviceCount;
    }

    public Set<Class<?>> getServiceInterfaces() {
        return serviceInterfaces;
    }

    public void setServiceInterfaces(Set<Class<?>> serviceInterfaces) {
        this.serviceInterfaces = serviceInterfaces;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "address='" + address + '\'' +
                ", lastHeartBeatTime=" + lastHeartBeatTime +
                '}';
    }
}
