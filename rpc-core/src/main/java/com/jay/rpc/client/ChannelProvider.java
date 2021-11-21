package com.jay.rpc.client;

import com.jay.rpc.transport.handler.RpcDecoder;
import com.jay.rpc.transport.handler.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * <p>
 *  连接Provider
 *  记录地址和对应的连接池
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
@Component
@Slf4j
public class ChannelProvider extends AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool> {
    private final NioEventLoopGroup group = new NioEventLoopGroup();

    @Resource
    private UnfinishedRequestHolder unfinishedRequestHolder;

    @Override
    protected SimpleChannelPool newPool(InetSocketAddress address) {
        // channel bootstrap
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(address);

        // simple连接池，无连接上限，无等待上限
        return new SimpleChannelPool(bootstrap, new RpcChannelPoolHandler());
    }

    /**
     * 连接池处理器
     * 处理连接释放、获取、创建事件
     */
    class RpcChannelPoolHandler implements ChannelPoolHandler {
        @Override
        public void channelReleased(Channel ch){
            // 释放channel时清空
            ch.flush();
        }

        @Override
        public void channelAcquired(Channel ch) {
            // channel获取成功
        }

        @Override
        public void channelCreated(Channel ch) {
            // 创建channel时，添加handler
            ChannelPipeline pipeline = ch.pipeline();
            /*
                编解码器
             */
            pipeline.addLast(new RpcEncoder());
            pipeline.addLast(new RpcDecoder());
            // 客户端handler
            pipeline.addLast(new ClientHandler(unfinishedRequestHolder));
        }
    }
}
