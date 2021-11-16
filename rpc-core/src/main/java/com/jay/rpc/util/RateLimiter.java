package com.jay.rpc.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * <p>
 *  令牌桶限流器
 *  匀速向桶中提供令牌，线程通过CAS获取令牌
 *
 *  这里为了减小开销，不使用一个单独线程来匀速提供token
 *  而是在每次获取时，计算与上一次补充token的时间间隔，用时间间隔 * 提供速率来补充token
 *
 *  案例推导：(mis=微秒，gap=稳定添加的时间间隔，rate=每次添加的数量)
 *  QPS = 1：1q/s -> 1q/1e6mis -> gap=1e6mis, rate=1
 *
 *  QPS = 60K：60Kq/s -> 6e-2q/mis -> ... -> gap=100mis, rate=6
 *
 *  QPS = 10K：10Kq/s -> 1e-2q/mis -> 1q/1e2mis -> gap=100mis, rate=1
 *
 *  QPS = 1M：1Mq/s -> .... -> 1q/mis -> gap=1mis, rate=1
 * </p>
 *
 * @author Jay
 * @date 2021/11/9
 **/
public class RateLimiter {
    private volatile int tokens;

    private static long tokenFiledOffset;
    /**
     *  稳定补充token的时间间隔
     *  比如QPS=1000, 换算到微秒为 10^-3 / mic, 换成整数：1 / 10^3 mic
     *  即 每1000微秒，提供一个token
     *  此时 stableInterval = 1000, rate = 1
     */
    private int stableInterval;
    /**
     * 补充速率
     */
    private int rate;
    /**
     * 桶容量
     */
    private int maxPermits;

    /**
     * mutex
     */
    private final Object mutex = new Object();
    /**
     * 上次补充token时间
     */
    private long lastSupplyTime;

    /**
     * Unsafe 用于tokens的CAS
     */
    private static Unsafe unsafe;
    /**
     * 默认CAS尝试次数
     */
    private static final int RETRY_TIMES = 10;

    /*
        获取Unsafe和tokens属性的偏移量
     */
    static{
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            tokenFiledOffset = unsafe.objectFieldOffset(RateLimiter.class.getDeclaredField("tokens"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private RateLimiter(int stableInterval, int rate, int maxPermits){
        this.stableInterval = stableInterval;
        this.rate = rate;
        this.maxPermits = maxPermits;
        this.tokens = maxPermits;
        // 获取当前微秒时间，纳秒 / 1e3
        this.lastSupplyTime = (long)Math.ceil(System.nanoTime() / 1e3);
    }

    /**
     * 创建RateLimiter
     * @param permitsPerSecond QPS
     * @return RateLimiter
     */
    public static RateLimiter create(int permitsPerSecond){
        if(permitsPerSecond < 1){
            throw new RuntimeException("permits per second must be positive");
        }
        // 转换时间单位为微秒
        double permitsPerUnit = permitsPerSecond / 1e6;
        // 计算稳定的时间间隔
        int interval = 1;
        while(permitsPerUnit < 1){
            permitsPerUnit *= 10;
            interval *= 10;
        }
        int rate = (int)permitsPerUnit;
        return new RateLimiter(interval, rate, permitsPerSecond);
    }

    private void supplyTokens(){
        synchronized (mutex){
            long currentMicTime = (long)Math.ceil(System.nanoTime() / 1e3);
            long supply = rate * (currentMicTime - lastSupplyTime) / stableInterval;
            // 提供token
            if(supply > 0){
                // 未超出容量
                if(tokens + supply <= maxPermits){
                    tokens += supply;
                }
                // 超出容量，直接填满
                else{
                    tokens = maxPermits;
                }
                // 记录供应时间
                lastSupplyTime = currentMicTime;
            }
        }
    }

    /**
     * 获取一个令牌，超时时间=0
     * @return boolean
     */
    public boolean tryAcquire(){
        return tryAcquire(1, 0, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取多个令牌
     * @param count 令牌数量
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return boolean
     */
    public boolean tryAcquire(int count, long timeout, TimeUnit timeUnit){
        // 获取前先补充token
        supplyTokens();
        // 重试次数
        int retry = 0;
        // 最大重试次数，如果超时时间为0，那么只能CAS一次
        int maxRetry = timeout == 0 ? 1 : RETRY_TIMES;
        // 每次重试等待时间
        long timeoutNanos = timeUnit.toNanos(timeout == 0 ? 1 : timeout);
        long oneParkTime = timeoutNanos / maxRetry;

        // 重试次数和超时时间
        while(timeoutNanos > 0 && retry < maxRetry){
            // CAS
            int expect = tokens;
            if(expect >= count && compareAndSwap(expect - count, expect)){
                return true;
            }
            // CAS失败，等待重试
            long parkStart = System.nanoTime();
            LockSupport.parkNanos(oneParkTime);
            long parkEnd = System.nanoTime();
            // 等待时长超过了一个间隔，获得一次supply机会
            if(parkEnd - parkStart > stableInterval){
                supplyTokens();
            }
            // 因为supply耗时的原因，这里要重新获取一次当前时间
            timeoutNanos = timeoutNanos - (System.nanoTime() - parkStart);
            retry++;
        }
        // 超时或重试多次失败
        return false;
    }

    /**
     * CAS tokens
     * @param value target value
     * @param expect expect value
     * @return boolean
     */
    private boolean compareAndSwap(int value, int expect){
        return unsafe.compareAndSwapInt(this, tokenFiledOffset, expect, value);
    }

    /**
     * 该方法将重置令牌桶的每秒允许令牌数
     * @param permitsPerSecond qps
     */
    public void setRate(int permitsPerSecond){
        if(permitsPerSecond <= 0){
            throw new IllegalArgumentException("permits per second must be positive");
        }
        synchronized (mutex){
            /*
                重置后，将当前桶内令牌设为0，使所有的线程暂时无法获得。
                之后在稳定的速率下逐渐填充。
                正在CAS的线程也会因为tokens和expect不同而重新尝试
             */
            this.tokens = 0;
            this.maxPermits = permitsPerSecond;
            // 设置上次提供时间为当前时间
            this.lastSupplyTime = (long)Math.ceil(System.nanoTime() / 1e3);
            // 转换时间单位为微秒
            double permitsPerUnit = permitsPerSecond / 1e6;
            // 重新计算稳定的时间间隔和速率
            int interval = 1;
            while(permitsPerUnit < 1){
                permitsPerUnit *= 10;
                interval *= 10;
            }
            this.stableInterval = interval;
            this.rate = (int)permitsPerUnit;
        }
    }
}
