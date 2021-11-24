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
    private String serviceName;
    private int serverCount;
    private int interfaceCount;
    private Set<Class<?>> serviceInterfaces;
    private List<ServerInfo> servers;
}
