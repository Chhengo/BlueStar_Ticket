package com.ticket.user.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("rateLimitScript")
    private final DefaultRedisScript<Long> rateLimitScript;

    @Qualifier("decrStockScript")
    private final DefaultRedisScript<Long> decrStockScript;

    // =================== 限流 ===================

    /**
     * 用户请求限流，60秒内最多 maxCount 次
     * @return true=允许通过，false=被限流
     */
    public boolean isAllowed(Long userId, int maxCount){
        String key = "ticket:rate:limit" + userId;
        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(maxCount)
        );
        return Long.valueOf(1L).equals(result);
    }
}
