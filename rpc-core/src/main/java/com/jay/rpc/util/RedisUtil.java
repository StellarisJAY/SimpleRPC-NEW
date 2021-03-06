package com.jay.rpc.util;

import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Redis工具类
 * </p>
 *
 * @author Jay
 * @date 2021/11/3
 **/
public class RedisUtil {
    /**
     * Jedis连接池
     */
    private JedisPool jedisPool;

    public RedisUtil(@Value("${rpc.service.registry.redis.host:localhost}") String host,
                     @Value("${rpc.service.registry.redis.port:6379}")int port,
                     @Value("${rpc.service.registry.redis.password}")String password,
                     @Value("${rpc.service.registry.redis.max-wait-millis:4000}")long maxWaitTime){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWaitMillis(maxWaitTime);
        this.jedisPool =  new JedisPool(poolConfig, host, port);
    }

    /**
     * get
     * @param key key
     * @return String
     */
    public String get(String key){
        try(Jedis jedis = jedisPool.getResource()){
            return jedis.get(key);
        }
    }

    public byte[] getBytes(String key){
        try(Jedis jedis = jedisPool.getResource()){
            return jedis.get(key.getBytes());
        }
    }

    /**
     * set
     * @param key key
     * @param value value
     */
    public void set(String key, String value){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.set(key, value);
        }
    }

    /**
     * set bytes
     * @param key key
     * @param value byte[]
     */
    public void set(String key, byte[] value){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.set(key.getBytes(), value);
        }
    }

    /**
     * setEx
     * @param key key
     * @param value value
     * @param expireTime ExpireTime
     * @param timeUnit TimeUnit
     */
    public void setEx(String key, String value, int expireTime, TimeUnit timeUnit){
        try(Jedis jedis = jedisPool.getResource()){
            long seconds = timeUnit.toSeconds(expireTime);
            jedis.setex(key, (int) seconds, value);
        }
    }

    /**
     * delete
     * @param key key
     */
    public void delete(String key){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.del(key);
        }
    }

    /**
     * keys
     * @param pattern pattern
     * @return Set
     */
    public Set<String> keys(String pattern){
        try(Jedis jedis = jedisPool.getResource()){
            return jedis.keys(pattern);
        }
    }

    protected JedisPool getJedisPool(){
        return jedisPool;
    }
}
