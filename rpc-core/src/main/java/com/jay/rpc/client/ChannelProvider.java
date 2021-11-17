package com.jay.rpc.client;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  记录 地址对应的channel
 * </p>
 *
 * @author Jay
 * @date 2021/11/17
 **/
@Component
public class ChannelProvider {
    /**
     * 记录地址-channel，避免每次建立TCP连接
     */
    private Map<String, Channel> channels = new ConcurrentHashMap<>(256);

    /**
     * get channel
     * @param address address
     * @return Channel
     */
    public Channel get(String address){
        Channel channel = channels.get(address);
        if(channel != null){
            if(!channel.isActive()){
                channels.remove(address);
            }
            else{
                return channel;
            }
        }
        return null;
    }

    /**
     * put channel
     * @param address address
     * @param channel channel
     */
    public void put(String address, Channel channel){
        /*
            channel不存在
         */
        if(!channels.containsKey(address)){
            channels.put(address, channel);
        }
        /*
            channel存在，根据channel状态判断是否覆盖
         */
        else{
            channels.compute(address, (key, originalValue)->{
                if(originalValue == null || !originalValue.isActive()){
                    return channel;
                }
                return originalValue;
            });
        }
    }

    public void remove(String address){
        channels.remove(address);
    }
}
