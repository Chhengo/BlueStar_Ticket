package com.ticket.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * 通用 RedisTemplate：key=String，value=JSON
     * 解决默认 JdkSerializationRedisSerializer 乱码问题
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer serializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // key 用 String 序列化
        template.setKeySerializer(serializer);
        template.setHashKeySerializer(serializer);
        // value 用 JSON 序列化
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
    //加载lua脚本 启动时只编译一次，用 SHA 调用，性能更好
    @Bean
    public DefaultRedisScript<Long> rateLimitScript(){
        //新建类
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
        //找到路径后返回指定类型
        defaultRedisScript.setLocation(new ClassPathResource("lua/RateLimitLua.lua"));
        defaultRedisScript.setResultType(Long.class);
        return defaultRedisScript;
    }

    @Bean
    public DefaultRedisScript<Long> StockDeductScript(){
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setLocation(new ClassPathResource("lua/StockDeduct.lua"));
        defaultRedisScript.setResultType(Long.class);
        return defaultRedisScript;
    }

    @Bean
    public DefaultRedisScript<Long> stockIncrementScript(){
        DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setLocation(new ClassPathResource("lua/StockIncrement.lua"));
        defaultRedisScript.setResultType(Long.class);
        return defaultRedisScript;
    }
    /**
     * Redis 配置：
     *  1. 开启 Keyspace Notification（监听 key 过期事件）
     *  2. 注册监听容器
     *
     * 注意：需要在 Redis 服务端开启 notify-keyspace-events Ex
     *  方式一（application.yml 中 Spring 自动配置）：
     *    spring.data.redis.notify-keyspace-events: Ex
     *  方式二（redis-cli）：
     *    CONFIG SET notify-keyspace-events Ex
     *
     * E = Keyevent events（key 事件通知）
     * x = 过期事件
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory, StringRedisTemplate stringRedisTemplate) {
        // 启动时自动配置 Redis 服务端开启 key 过期通知
        stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .setConfig("notify-keyspace-events", "Ex");

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }
}
