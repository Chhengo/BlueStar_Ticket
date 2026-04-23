package com.ticket.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class StockService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Qualifier("StockDeductScript")
    @Autowired
    private DefaultRedisScript<Long> stockDeductScript;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    //注入redis客户端 库存自扣减脚本
    //方法怎么写？ 入参 当前活动id 票id 在redis里操作
    //组装key  原先key为ticket:stock:{eventId}:{ticketId}
    //运行lua 组装longresult匹配是否正常扣减
    //用redis注入实体调用execute执行方法
    public boolean stockDeductScript(Long eventId, Long ticketId){
        //包装在key里
        //key不一样导致库存无法扣减 eventId 改为 stock 少了一个冒号。。
        String key = "ticket:stock:" + eventId + ":" + ticketId;
        //活动id和票id
        //执行脚本
        Long result = stringRedisTemplate.execute(
                stockDeductScript,
                Collections.singletonList(key) //为啥不能直接放个key
        );
        return Long.valueOf(1L).equals(result);
    }
}
