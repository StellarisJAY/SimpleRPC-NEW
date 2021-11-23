package com.jay.rpc.registry.impl;

import com.jay.common.extention.ExtensionLoader;
import com.jay.rpc.entity.ServerInfo;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.transport.serialize.Serializer;
import com.jay.rpc.transport.serialize.protostuff.ProtoStuffSerializer;
import com.jay.rpc.util.ZookeeperUtil;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *   Zookeeper服务发现
 * </p>
 *
 * @author Jay
 * @date 2021/10/28
 **/
public class ZooKeeperRegistry extends Registry {

    private static final String PATH_PREFIX = "/rpc/services";

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    /**
     * ZooKeeper工具
     * 包括ZooKeeper连接，以及各种操作的封装
     */
    @Resource
    private ZookeeperUtil zookeeperUtil;


    /**
     * 通过该构造方法创建服务注册中心
     * @param zookeeperUtil zkUtil
     */
    public ZooKeeperRegistry(ZookeeperUtil zookeeperUtil) {
        this.zookeeperUtil = zookeeperUtil;
    }

    /**
     * 注册服务
     * @param applicationName 服务名
     * @param address 服务地址
     * @throws KeeperException Zookeeper异常
     * @throws InterruptedException 打断异常
     */
    @Override
    public void registerService(String applicationName, String address) throws Exception {
        if(StringUtils.isEmpty(applicationName)){
            throw new IllegalArgumentException("服务名不允许为空");
        }
        // 服务根路径
        String serviceRootPath = PATH_PREFIX + "/" + applicationName;
        // 服务地址路径，临时节点，服务连接断开就释放
        String serviceAddrPath = serviceRootPath + "/" + address;

        if(zookeeperUtil.exists(serviceAddrPath)){
            throw new RuntimeException("服务名已被注册");
        }

        // 生成服务器信息
        ServerInfo serverInfo = getApplicationInfo(applicationName, address);
        // 序列化
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("json");
        String serializedInfo = new String(serializer.serialize(serverInfo));
        // 服务根目录
        if(!zookeeperUtil.exists(serviceRootPath)){
            zookeeperUtil.createPersistent(serviceRootPath, "root", ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }
        // 创建服务地址-临时节点，data记录主机信息
        zookeeperUtil.createEphemeral(serviceAddrPath, serializedInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    @Override
    public void heartBeat(String applicationName, String address) {
        ServerInfo serverInfo = getApplicationInfo(applicationName, address);
        // 序列化
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("json");
        String serialized = new String(serializer.serialize(serverInfo));
        try {
            zookeeperUtil.setData(PATH_PREFIX + "/" + applicationName + "/" + address, serialized);
            logger.info("心跳，更新服务状态成功");
        } catch (Exception e) {
            logger.error("心跳出现异常", e);
            throw new RuntimeException("心跳异常");
        }
    }

    /**
     * 服务发现
     * @return Zookeeper中的所有服务
     */
    @Override
    public Map<String, List<ServerInfo>> discoverService(){
        try {
            // 获取所有服务名
            List<String> applicationNames = zookeeperUtil.listChildren(PATH_PREFIX);
            Map<String, List<ServerInfo>> result = new HashMap<>(16);
            for(String applicationName : applicationNames){
                // 获取服务名对应的list
                List<ServerInfo> servers = result.computeIfAbsent(applicationName, k->new ArrayList<>());
                // 获取服务名下的地址
                List<String> addresses = zookeeperUtil.listChildren(PATH_PREFIX + "/" + applicationName);
                for(String address : addresses){
                    // 获取该地址对应的服务器信息
                    String serializedInfo = zookeeperUtil.getData(PATH_PREFIX + "/" + applicationName + "/" + address);
                    // 反序列化
                    Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("json");
                    ServerInfo serverInfo = serializer.deserialize(serializedInfo.getBytes(), ServerInfo.class);
                    serverInfo.setAlive(true);
                    // 写入结果
                    servers.add(serverInfo);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("服务发现出现异常", e);
            throw new RuntimeException("服务发现出现异常");
        }
    }

    /**
     * 获取服务地址
     * @param serviceName 服务名
     * @return String
     */
    @Override
    public List<InetSocketAddress> getServiceAddress(String serviceName){
        try{
            String servicePath = PATH_PREFIX + "/" + serviceName;
            // 获取服务名下的所有地址
            List<String> children = zookeeperUtil.listChildren(servicePath);

            return children.stream().map((address) -> {
                // 解析地址字符串，返回InetSocketAddress
                String ip = address.substring(0, address.indexOf(":"));
                int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
                return new InetSocketAddress(ip, port);
            }).collect(Collectors.toList());
        }catch (Exception e){
            logger.error("获取服务地址出现异常", e);
            return null;
        }
    }
}
