package com.example.demo.redis.redissonStater;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson bean管理
 * @author Jashin
 */
@Configuration
public class RedissonConfig {

    /**
     * Redisson客户端注册
     * 单机模式
     */
    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient createRedissonClient() throws Exception{

//       Config config = new Config();
//        SingleServerConfig singleServerConfig = config.useSingleServer();
//        singleServerConfig.setAddress("redis://127.0.0.1:6379");
//        singleServerConfig.setPassword("12345");
//        singleServerConfig.setTimeout(3000);
//        return Redisson.create(config)

        // 本例子使用的是yaml格式的配置文件，读取使用Config.fromYAML，如果是Json文件，则使用Config.fromJSON
        Config config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-config.yml"));
        return Redisson.create(config);
    }

//    /**
//     * 主从模式 哨兵模式
//     *
//     **/
//    @Bean
//    public RedissonClient getRedisson(){
//        RedissonClient redissonClient;
//        Config config = new Config();
//        config.useMasterSlaveServers()
//                //可以用"rediss://"来启用SSL连接
//                //设置主服务器IP
//                .setMasterAddress("redis://127.0.0.1:6379").setPassword(null)
//                .setRetryInterval(5000)
//                //（连接超时，单位：毫秒 默认值：3000）;
//                .setConnectTimeout(10000)
//                .setTimeout(10000)
//                //设置从服务器IP
//                .setSlaveAddresses(new HashSet<>(Arrays.asList("redis://127.0.0.1:6379","redis://127.0.0.1:6379")));
//        //  哨兵模式config.useSentinelServers().setMasterName("mymaster").setPassword("web2017").addSentinelAddress("***(哨兵IP):26379", "***(哨兵IP):26379", "***(哨兵IP):26380");
//        redissonClient = Redisson.create(config);
//        return redissonClient;
//    }

    /**
     * 将RedissonDistributeLocker 交给Spring管理
     * @param redissonClient
     * @return
     */
    @Bean
    public RedissonDistributeLocker redissonLocker(RedissonClient redissonClient){
        RedissonDistributeLocker locker = new RedissonDistributeLocker(redissonClient);
        RedissonLockUtils.setLocker(locker);
        return locker;
    }
}
