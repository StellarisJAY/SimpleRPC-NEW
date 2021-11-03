package com.jay.rpc.registry.impl;

import com.jay.rpc.registry.IRegistry;
import com.jay.rpc.util.ZookeeperUtil;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *   Zookeeper服务发现
 * </p>
 *
 * @author Jay
 * @date 2021/10/28
 **/
public class ZooKeeperRegistry implements IRegistry {

    private static final String PATH_PREFIX = "/rpc/services";

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    private ZookeeperUtil zookeeperUtil;


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

        if(!zookeeperUtil.exists(serviceRootPath)){
            // 节点不存在，创建服务信息-持久节点
            zookeeperUtil.createPersistent(serviceRootPath, "info", ZooDefs.Ids.OPEN_ACL_UNSAFE);
        }else{
            // 更新服务信息
        }
        // 创建服务地址-临时节点
        zookeeperUtil.createEphemeral(serviceAddrPath, address, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    /**
     * 服务发现
     * @return Zookeeper中的所有服务
     */
    @Override
    public List<String> discoverService(){
        try {
            return zookeeperUtil.listChildren(PATH_PREFIX);
        } catch (KeeperException | InterruptedException e) {
            logger.error("服务发现出现异常", e);
            return null;
        }
    }

    /**
     * 获取服务地址
     * @param serviceName 服务名
     * @return String
     */
    @Override
    public String getServiceAddress(String serviceName){
        try{
            String servicePath = PATH_PREFIX + "/" + serviceName;
            return zookeeperUtil.getData(servicePath + "/address");
        }catch (Exception e){
            logger.error("获取服务地址出现异常", e);
            return null;
        }
    }
}
