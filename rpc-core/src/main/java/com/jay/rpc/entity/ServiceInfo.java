package com.jay.rpc.entity;

import lombok.*;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *   服务信息实体
 *   包括了服务名、服务接口、服务器列表
 * </p>
 *
 * @author Jay
 * @date 2021/11/24
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceInfo {
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 活跃服务器数量
     */
    private int serverCount;
    /**
     * 服务器列表
     */
    private List<ServerInfo> servers;
    /**
     * 接口数量
     */
    private int interfaceCount;
    /**
     * 接口列表
     */
    private Set<Class<?>> serviceInterfaces;

}
