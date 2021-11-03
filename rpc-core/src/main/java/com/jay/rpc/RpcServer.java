package com.jay.rpc;

import com.jay.rpc.annotation.RpcService;
import com.jay.rpc.discovery.ServiceMapper;
import com.jay.rpc.registry.IRegistry;
import com.jay.rpc.registry.impl.ZooKeeperRegistry;
import com.jay.rpc.handler.RpcDecoder;
import com.jay.rpc.handler.RpcEncoder;
import com.jay.rpc.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  RPC 服务器
 * </p>
 *
 * @author Jay
 * @date 2021/10/13
 **/
public class RpcServer implements ApplicationContextAware {
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${rpc.service.port}")
    private String port;

    @Value("${spring.application.name}")
    private String applicationName;

    private ApplicationContext context;

    @Resource
    private IRegistry serviceRegistry;

    /**
     * 初始化Netty服务器
     * @return ServerBootstrap
     */
    private ServerBootstrap init(){
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel){
                        /*
                            处理器管线
                         */
                        ChannelPipeline pipeline = channel.pipeline();
                        // Rpc解码器
                        pipeline.addLast(new RpcDecoder());
                        // Rpc请求处理器
                        pipeline.addLast(new RpcRequestHandler(context));
                        // Rpc编码器
                        pipeline.addLast(new RpcEncoder());
                    }
                });
        return serverBootstrap;
    }

    /**
     * 构造方法后自动启动服务器
     */
    @PostConstruct
    public void start(){
        // 初始化服务器属性
        ServerBootstrap serverBootstrap = init();

        try {
            logger.info("RPC服务启动中...");
            // 获取服务地址
            InetAddress localHost = InetAddress.getLocalHost();
            String host = localHost.getHostAddress() + ":" + port;
            // 注册到Zookeeper
            serviceRegistry.registerService(applicationName, host);
            logger.info("服务注册成功，服务名称：{}", applicationName);

            int serviceCount = doServiceScan();
            logger.info("接口实现类扫描完成，一共扫描到：{} 个服务实现类Bean", serviceCount);

            /*
                如果没有扫描到服务实现类，表示该应用只作为客户端，不必启动服务器
             */
            if(serviceCount != 0){
                // 启动服务器
                ChannelFuture channelFuture = serverBootstrap.bind(Integer.parseInt(port)).sync();
                if(channelFuture.isSuccess()){
                    logger.info("RPC服务启动成功，服务地址:{}", host);
                }
                else{
                    logger.info("RPC服务启动失败");
                }
            }
        }catch (Exception e){
            logger.error("服务启动异常", e);
        }
    }

    /**
     * 提供Spring Bean容器
     * @param applicationContext appContext
     * @throws BeansException BeanException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(applicationContext != null){
            this.context = applicationContext;
        }
    }

    /**
     * 扫描服务实现类
     * @return 服务Bean数量
     */
    private int doServiceScan(){
        if(this.context != null){
            // 获取有@RpcService的Bean
            Map<String, Object> serviceImpls = context.getBeansWithAnnotation(RpcService.class);
            // 遍历，将这些Bean放入ServiceMapper
            Set<Map.Entry<String, Object>> entries = serviceImpls.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                Object bean = entry.getValue();
                Class<?>[] interfaces = bean.getClass().getInterfaces();
                ServiceMapper.put(interfaces[0], bean);
            }
            return entries.size();
        }
        return 0;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
