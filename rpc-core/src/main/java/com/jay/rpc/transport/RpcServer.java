package com.jay.rpc.transport;

import com.jay.rpc.annotation.RpcService;
import com.jay.rpc.discovery.ServiceMapper;
import com.jay.rpc.transport.handler.*;
import com.jay.rpc.transport.handler.filter.TrafficControlFilter;
import com.jay.rpc.registry.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.HashSet;
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
@Component
@Slf4j
public class RpcServer implements ApplicationContextAware {
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
    /**
     * RPC服务器端口
     */
    @Value("${rpc.service.port:9000}")
    private String port;

    /**
     * RPC服务名
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 是否开启服务端限流
     */
    @Value("${rpc.traffic.enable-control:true}")
    private boolean enableTrafficControl;
    /**
     * 限流QPS
     */
    @Value("${rpc.traffic.permits-per-second:1000}")
    private int permitsPerSecond;

    private ApplicationContext context;

    /**
     * 用户自定义过滤器集合
     */
    private Set<Filter> filters;
    /**
     * 注册中心实例
     */
    @Resource
    private Registry serviceRegistry;

    /**
     * 初始化Netty服务器
     * @return ServerBootstrap
     */
    private ServerBootstrap init(){
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel){
                        /*
                            处理器管线
                         */
                        ChannelPipeline pipeline = channel.pipeline();
                        // Rpc解码器
                        pipeline.addLast(new RpcDecoder());
                        // 限流器
                        if(enableTrafficControl){
                            pipeline.addLast(new TrafficControlFilter(permitsPerSecond));
                        }
                        // 注册用户自定义过滤器
                        for(Filter filter : filters){
                            pipeline.addLast(filter);
                        }
                        // Rpc请求处理器
                        pipeline.addLast(new RpcRequestHandler());

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
            log.info("RPC服务启动中...");
            // 服务地址
            InetAddress localHost = InetAddress.getLocalHost();
            String host = localHost.getHostAddress() + ":" + port;
            // 注册到服务注册中心
            serviceRegistry.registerService(applicationName, host);
            // 开启心跳
            serviceRegistry.startHearBeat(applicationName, host);
            log.info("服务注册成功，服务名称：{}", applicationName);

            int serviceCount = doServiceScan();
            log.info("接口实现类扫描完成，一共扫描到：{} 个服务实现类Bean", serviceCount);

            /*
                如果没有扫描到服务实现类，表示该应用只作为客户端，不必启动服务器
             */
            if(serviceCount != 0){
                // 启动服务器
                ChannelFuture channelFuture = serverBootstrap.bind(Integer.parseInt(port)).sync();
                if(channelFuture.isSuccess()){
                    log.info("RPC服务启动成功，服务地址:{}", host);
                }
                else{
                    log.info("RPC服务启动失败");
                }
            }
        }catch (Exception e){
            log.error("服务启动异常", e);
        }
    }

    /**
     * 提供Spring Bean容器
     * @param applicationContext appContext
     * @throws BeansException BeanException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        // 获取用户自定义Filter
        Map<String, Filter> filters = applicationContext.getBeansOfType(Filter.class);
        log.info("共发现自定义过滤器：{} 个", filters.size());
        this.filters = new HashSet<>(filters.values());
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
}
