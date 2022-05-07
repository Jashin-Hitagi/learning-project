package com.example.demo.redis.redissonStater;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * redisson实现分布式锁接口
 * 读+读
 * 相当于无锁，并发读，只会在redis中记录好，所有当前的锁，都会同时加锁成功
 *
 * 写+读
 * 等待写锁释放
 *
 * 写+写
 * 阻塞等待
 *
 * 读+写
 * 有读锁，写也需要等待
 *
 * 总结就是一句话：只要有写的存在，都必须等待
 * @author Jashin
 */
@RequiredArgsConstructor
//@Component
public class RedissonDistributeLocker implements DistributeLocker{

    private final RedissonClient redissonClient;

    @Override
    public void lock(String lockKey) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        rLock.writeLock().lock();
    }

    @Override
    public void unlock(String lockKey) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        rLock.writeLock().unlock();
    }

    @Override
    public void lock(String lockKey, long timeout) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        rLock.writeLock().lock(timeout,TimeUnit.MICROSECONDS);
    }

    @Override
    public void lock(String lockKey, long timeout, TimeUnit unit) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        rLock.writeLock().lock(timeout, unit);
    }

    @Override
    public boolean tryLock(String lockKey) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        return rLock.writeLock().tryLock();
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        return rLock.writeLock().tryLock(waitTime,leaseTime,unit);
    }

    @Override
    public boolean isLocked(String lockKey) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        return rLock.writeLock().isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String lockKey) {
        RReadWriteLock rLock = redissonClient.getReadWriteLock(lockKey);
        return rLock.writeLock().isHeldByCurrentThread();
    }

}
