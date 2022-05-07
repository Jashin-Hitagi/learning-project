package com.example.demo.redis.redissonStater;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    @PostMapping(value = "testLock", consumes = "application/json")
    @RedissonLock(lockRedisKey = "productName,platFormName")
    public String testLock(@RequestBody JSONObject params) throws InterruptedException {
        /**
         * 分布式锁key=params.getString("productName")+params.getString("platFormName");
         * productName 产品名称  platFormName 平台名称 如果都一致,那么分布式锁的key就会一直,那么就能避免并发问题
         */
        //TODO 业务处理

        System.out.println("接收到的参数："+params.toString());
        System.out.println("执行相关业务...");
        System.out.println("执行相关业务.....");

        System.out.println("执行相关业务......");

        return "success";
    }

}
