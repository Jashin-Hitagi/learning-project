package com.example.demo.redis.redissonStater;

import java.util.concurrent.TimeUnit;

/**
 * @author Jashin
 */
public interface DistributeLocker {

    /**
     * 加锁
     * @param lockKey key
     */
    void lock(String lockKey);

    /**
     * 释放锁
     *
     * @param lockKey key
     */
    void unlock(String lockKey);

    /**
     * 加锁锁,设置有效期 releaseTime
     * 设置releaseTime=-1时，Redisson并不是不设置锁的持有时间，而是默认设置了30s，然后通过netty的定时任务每10s就去进行续期，续期长度是30s
     * @param lockKey key
     * @param timeout 有效时间，默认时间单位在实现类传入
     */
    void lock(String lockKey, long timeout);

    /**
     * 加锁，设置有效期并指定时间单位
     * @param lockKey key
     * @param timeout 有效时间
     * @param unit    时间单位
     */
    void lock(String lockKey, long timeout, TimeUnit unit);

    /**
     * 尝试获取锁，获取到则持有该锁返回true,未获取到立即返回false
     * @param lockKey
     * @return true-获取锁成功 false-获取锁失败
     */
    boolean tryLock(String lockKey);

    /**
     *  尝试加锁，最多等待waitTime，上锁以后leaseTime自动解锁
     * 若未获取到，在waitTime时间内一直尝试获取，超过waitTime还未获取到则返回false
     * @param lockKey   key
     * @param waitTime  尝试获取时间
     * @param leaseTime 锁持有时间
     * @param unit      时间单位
     * @return true-获取锁成功 false-获取锁失败
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit)
            throws InterruptedException;

    /**
     * 锁是否被任意一个线程锁持有
     * @param lockKey
     * @return true-被锁 false-未被锁
     */
    boolean isLocked(String lockKey);

    /**
     * 查询当前线程是否保持此锁定
     * lock.isHeldByCurrentThread()
     * @param lockKey
     * @return
     */
    boolean isHeldByCurrentThread(String lockKey);

}
