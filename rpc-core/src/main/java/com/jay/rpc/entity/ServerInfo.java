package com.jay.rpc.entity;

import lombok.*;

import java.util.Set;

/**
 * <p>
 *
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

    private String address;
    private long lastHeartBeatTime;
    private boolean alive;

    private long maxMemory;
    private int availableProcessors;
}
