package com.example.demo.redis.apiLimiting;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Jashin
 * 注解标明需要使用限流的接口
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {

    /**
     * 指定second 时间内 API请求次数
     */
    int seconds() default 5;

    /**
     * 请求次数的指定时间范围  秒数(redis数据过期时间)
     */
    int maxCount() default 60;

}
