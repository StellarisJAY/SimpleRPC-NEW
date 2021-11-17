package com.jay.rpc.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * <p>
 *  Redis不可重入独占锁
 * </p>
 *
 * @author Jay
 * @date 2021/11/16
 **/
public class RedisLock {
    @Resource
    private RedisUtil redisUtil;

    private static final long SINGLE_PARK_TIME = 100;

    private static final int LOCK_EXPIRE_TIME = 30;

    /**
     * 解锁操作lua脚本
     * 先id是否和锁的id相同
     * 相同则释放锁
     */
    private static final String UNLOCK_SCRIPT =
                    "if(redis.call('get', KEYS[1]) == ARGV[1]) then " +
                    "   redis.call('del', KEYS[1]); " +
                    "   return 1;" +
                    "end; " +
                    "return 0;";

    /**
     * 加锁，阻塞
     * @param name 锁名称
     * @param id 加锁主机id
     * @throws InterruptedException e
     */
    public void lock(String name, String id) throws InterruptedException {
        JedisPool jedisPool = redisUtil.getJedisPool();
        try(Jedis jedis = jedisPool.getResource()){
            while(true){
                // set if not exists & expire
                String status = jedis.set(name, id, "NX", "EX", LOCK_EXPIRE_TIME);
                // 加锁成功
                if("OK".equalsIgnoreCase(status)){
                    return;
                }
                // 失败，等待
                Thread.sleep(SINGLE_PARK_TIME);
            }
        }
    }

    /**
     * 尝试加锁，带超时时间
     * @param name 锁name
     * @param id 主机id
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return boolean
     */
    public boolean tryLock(String name, String id, long timeout, TimeUnit timeUnit){
        JedisPool jedisPool = redisUtil.getJedisPool();
        try(Jedis jedis = jedisPool.getResource()){
            long timeoutNanos = timeUnit.toNanos(timeout);
            // 自旋
            while(timeoutNanos > 0){
                // set if not exists & expire
                String status = jedis.set(name, id, "NX", "EX", LOCK_EXPIRE_TIME);
                // 加锁成功
                if("OK".equalsIgnoreCase(status)){
                    return true;
                }
                // 失败，等待
                long parkStart = System.nanoTime();
                LockSupport.parkNanos(SINGLE_PARK_TIME);
                // 减少时间
                timeoutNanos -= System.nanoTime() - parkStart;
            }
            return false;
        }
    }

    /**
     * 解锁
     * 因为解锁有判断id是否相同和删除两步，所以需要用lua脚本实现原子化
     * @param name name
     * @param id id
     */
    public void unlock(String name, String id){
        JedisPool jedisPool = redisUtil.getJedisPool();
        try(Jedis jedis = jedisPool.getResource()){
            // 执行lua脚本
            if((Long)jedis.eval(UNLOCK_SCRIPT, Collections.singletonList(name), Collections.singletonList(id)) == 0){
                throw new IllegalMonitorStateException("lock is held by another server");
            }
        }
    }
}
