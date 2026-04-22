package com.ticket.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DefaultRedisScript<Long> rateLimitScript;

    /**
     * @param key      限流维度，比如 "grab:event:1" 或 "grab:user:123"
     * @param rate     每秒放令牌数
     * @param capacity 桶最大容量
     * @return true=允许通过，false=被限流
     */
    public boolean isAllowed(String key, int rate, int capacity) {
        List<String> keys = Arrays.asList(
                "rate_limit:" + key + ":tokens",
                "rate_limit:" + key + ":last_time"
        );

        long now = System.currentTimeMillis();

        Long result = redisTemplate.execute(
                rateLimitScript,
                keys,
                String.valueOf(rate),
                String.valueOf(capacity),
                String.valueOf(now),
                "1"   // 每次请求消耗1个令牌
        );

        return Long.valueOf(1L).equals(result);
    }
}
