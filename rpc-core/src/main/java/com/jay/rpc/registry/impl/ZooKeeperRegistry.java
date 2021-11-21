package com.jay.rpc.registry.impl;

import com.jay.rpc.entity.ApplicationInfo;
import com.jay.rpc.registry.Registry;
import com.jay.rpc.transport.serialize.protostuff.ProtoStuffSerializer;
import com.jay.rpc.util.ZookeeperUtil;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

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
        String serviceAddrPath = serviceRootPath + "/address";

        if(zookeeperUtil.exists(serviceAddrPath)){
            throw new RuntimeException("服务名已被注册");
        }

        // 生成服务器信息
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        // 序列化
        String serializedInfo = ProtoStuffSerializer.serializeJSON(applicationInfo);
        // 写入ZooKeeper
        if(!zookeeperUtil.exists(serviceRootPath)){
            // 节点不存在，创建服务信息-持久节点
            zookeeperUtil.createPersistent(serviceRootPath, serializedInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }else{
            // 更新服务信息
            zookeeperUtil.setData(serviceRootPath, serializedInfo);
        }
        // 创建服务地址-临时节点
        zookeeperUtil.createEphemeral(serviceAddrPath, address, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    @Override
    public void heartBeat(String applicationName, String address) {
        ApplicationInfo applicationInfo = getApplicationInfo(applicationName, address);
        String serialized = ProtoStuffSerializer.serializeJSON(applicationInfo);
        try {
            zookeeperUtil.setData(PATH_PREFIX + "/" + applicationName, serialized);
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
    public List<ApplicationInfo> discoverService(){
        try {
            List<String> applicationNames = zookeeperUtil.listChildren("/rpc/services");
            List<ApplicationInfo> infos = new ArrayList<>(applicationNames.size());
            for (String applicationName : applicationNames) {
                // 获取 info 节点数据
                String serializedInfo = zookeeperUtil.getData(PATH_PREFIX + "/" + applicationName);
                ApplicationInfo info = ProtoStuffSerializer.deserializeJSON(serializedInfo, ApplicationInfo.class);
                // 判断是否存在地址节点，即检查节点是否还存活
                boolean alive = zookeeperUtil.exists(PATH_PREFIX + "/" + applicationName + "/address");
                info.setAlive(alive);
                infos.add(info);
            }
            return infos;
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
    public InetSocketAddress getServiceAddress(String serviceName){
        try{
            String servicePath = PATH_PREFIX + "/" + serviceName;
            String address = zookeeperUtil.getData(servicePath + "/address");
            String ip = address.substring(0, address.indexOf(":"));
            int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
            return new InetSocketAddress(ip, port);
        }catch (Exception e){
            logger.error("获取服务地址出现异常", e);
            return null;
        }
    }
}
