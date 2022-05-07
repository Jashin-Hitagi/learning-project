package com.example.demo.redis.redissonStater;

import com.alibaba.fastjson.JSONObject;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 * @Aspect 描述的类为切面类，此类中实现：
 * 1)切入点(Pointcut)的定义
 * 2)通知(advice)的定义(扩展功能)
 */
@Aspect
@Component
@Slf4j
public class RedissonLockAop {

    /**
     * @Pointcut 注解用于描述或定义一个切入点
     *  切入点的定义需要遵循spring中指定的表达式规范
     *  bean(bean名称或一个表达式) || 注解路径
     */
    @Pointcut("@annotation(com.example.demo.redis.redissonStater.RedissonLock)")
    public void redissonLockPoint(){
    }

    /**
     * @Around 注解描述的方法为一个环绕通知方法，
     * 在此方法中可以添加扩展业务逻辑，可以调用下一个
    切面对象或目标方法
     * @param pjp 连接点(此连接点只应用@Around描述的方法)
     * @return
     * @throws Throwable
     */
    @Around("redissonLockPoint()")
    @ResponseBody
    public String checkLock(ProceedingJoinPoint pjp) throws Throwable{
        String threadName = Thread.currentThread().getName();
        log.info("线程{}------进入分布式锁aop------", threadName);
        //获取参数列表
        Object[] objs = pjp.getArgs();
        //获取该注解的实例对象
        //获取方法签名(通过此签名获取目标方法信息)
        RedissonLock annotation = ((MethodSignature) pjp.getSignature())
                .getMethod().getAnnotation(RedissonLock.class);
        //生成分布式锁key的键名，以逗号分隔
        String lockRedisKey = annotation.lockRedisKey();
        StringBuilder keyBuffer = new StringBuilder();
        if (StringUtil.isEmpty(lockRedisKey)) {
            log.info("线程{} lockRedisKey设置为空，不加锁", threadName);
            //执行目标方法
            pjp.proceed();
            return "NULL LOCK";
        }else {
            JSONObject param = (JSONObject) objs[0];
            //生成分布式锁key
            String[] keyPartArr = lockRedisKey.split(",");
            for (String keyPart : keyPartArr){
                keyBuffer.append(param.getString(keyPart));
            }
            String key = keyBuffer.toString();
            log.info("线程{} 锁的key={}", threadName, key);
            //获取锁  3000 等到获取锁的时间  leaseTime 获取锁后持有时间   时间单位 MILLISECONDS：毫秒
            if (RedissonLockUtils.tryLock(key, 3000, 5000, TimeUnit.MILLISECONDS)) {
                try {
                    log.info("线程{} 获取锁成功", threadName);
                    return (String) pjp.proceed();
                } finally {
                    if (RedissonLockUtils.isLocked(key)) {
                        log.info("key={}对应的锁被持有,线程{}",key, threadName);
                        if (RedissonLockUtils.isHeldByCurrentThread(key)) {
                            log.info("当前线程 {} 保持锁定", threadName);
                            RedissonLockUtils.unlock(key);
                            log.info("线程{} 释放锁", threadName);
                        }
                    }
                }
            } else {
                log.info("线程{} 获取锁失败", threadName);
                return " GET LOCK FAIL";
            }
        }
    }

}
