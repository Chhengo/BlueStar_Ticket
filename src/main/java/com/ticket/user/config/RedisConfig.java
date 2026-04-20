package com.ticket.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
        return template;}

    // ===================== Lua 脚本 Bean =====================

    /**
     * 令牌桶限流脚本
     * KEYS[1]: ticket:rate:limit:{userId}
     * ARGV[1]: 限制次数（如 5）
     * 返回: 1=允许, 0=拒绝
         */
    @Bean("rateLimitScript")
    public DefaultRedisScript<Long> rateLimitScript(){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
            "local count = redis.call('INCR', KEYS[1]) " +
            "if count == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], 60) " +
            "end " +
            "if tonumber(count) > tonumber(ARGV[1]) then " +
            "    return 0 " +
            "end " +
            "return 1"
        );
        return script;
    }

    /**
     * 原子扣库存脚本
     * KEYS[1]: ticket:stock:{ticketTypeId}
     * ARGV[1]: 购买数量
     * 返回: 1=成功, 0=库存不足
     */
    @Bean("decrStockScript")
    public DefaultRedisScript<Long> defaultRedisScript(){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
            "local stock = tonumber(redis.call('GET',KEYS[1]))" +
            "if stock == nil or stock < tonumber(ARGV[1]) then" +
            " return 0" +
            "end " +
            "redis.call('DECRBY', KEYS[1], ARGV[1])" +
            "return 1"
        );
        return script;
    }
}
