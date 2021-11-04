package com.jay.rpc.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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

    public RedisUtil(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * get
     * @param key key
     * @return String
     */
    public String get(String key){
        Jedis jedis = jedisPool.getResource();
        return jedis.get(key);
    }

    /**
     * set
     * @param key key
     * @param value value
     */
    public void set(String key, String value){
        Jedis jedis = jedisPool.getResource();
        jedis.set(key, value);
    }

    /**
     * setEx
     * @param key key
     * @param value value
     * @param expireTime ExpireTime
     * @param timeUnit TimeUnit
     */
    public void setEx(String key, String value, int expireTime, TimeUnit timeUnit){
        Jedis jedis = jedisPool.getResource();
        long seconds = timeUnit.toSeconds(expireTime);
        jedis.setex(key, (int) seconds, value);
    }

    /**
     * delete
     * @param key key
     */
    public void delete(String key){
        Jedis jedis = jedisPool.getResource();
        jedis.del(key);
    }
}
