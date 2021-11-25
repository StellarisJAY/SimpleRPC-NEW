package com.jay.rpc.entity;

import lombok.*;


/**
 * <p>
 *  服务器信息
 * </p>
 *
 * @author Jay
 * @date 2021/11/5
 **/
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerInfo {
    /**
     * 地址 ip:port
     */
    private String address;
    /**
     * 上次心跳时间
     */
    private long lastHeartBeatTime;
    /**
     * 是否存活
     */
    private boolean alive;

    /**
     * maxMem
     */
    private long maxMemory;
    /**
     * CPUs
     */
    private int availableProcessors;
}
