package com.ticket.redis.service;

import com.ticket.events.entity.TicketType;
import com.ticket.events.mapper.TicketsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 预缓存库存 防止qps压垮redis
 */
@Component //自动管理创建成 Bean
@RequiredArgsConstructor //自动生成构造方法
public class StockInitializer implements CommandLineRunner {
    private final TicketsMapper ticketsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public void run(String... args) throws Exception {
        List<TicketType> allData = ticketsMapper.getAllData();
        for(TicketType t : allData){
            String redisKey = "ticket:stock:"+ t.getEventId() + ":"+ t.getId();
            //活动与票id
            redisTemplate.opsForValue().set(redisKey,t.getTotalStock());
        }
    }
}
