package com.ticket.pay.service.impl;

import com.ticket.orders.entity.Order;
import com.ticket.orders.mapper.OrderMapper;
import com.ticket.pay.service.PayService;
import com.ticket.redis.config.RedisKeyBuild;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PayServiceImpl implements PayService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * P1 · 模拟支付接口
     *
     * 校验规则：
     *  1. 订单必须存在
     *  2. 订单 userId 必须与请求 userId 一致（防越权）
     *  3. status 必须为 0（待支付）
     *  4. expire_time 未过（15分钟内）
     *
     * 支付成功后：
     *  - status → 1，写 pay_time
     *  - 删除 Redis 过期监听 key（防止已支付订单触发超时回补）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo, Long userId) {
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if(order == null){
            throw new IllegalArgumentException("订单不存在:" + orderNo);
        }if(order.getUserId() != userId){
            throw new IllegalArgumentException("无权操作该订单");
        }if(order.getStatus() == 1){
            throw new IllegalStateException("订单已支付");
        }
        // ⑥ 模拟支付成功：更新 status=1，写 pay_time
        log.info("[Pay] 正在支付中（模拟支付）");
        int updated = orderMapper.payOrder(orderNo, LocalDateTime.now());
        if(updated == 0){
            throw new IllegalStateException("支付失败，请稍后重试");
        }
        // ⑦ 删除 Redis 过期监听 key，防止已支付订单触发超时回补
        String expireWatchKey = RedisKeyBuild.expireWatchKey(
                order.getEventId(),order.getTicketId(),
                order.getUserId(),order.getOrderNo());
        Boolean deleted = stringRedisTemplate.delete(expireWatchKey);
        log.info("[Pay] 删除过期监听key：key={},deleted={}",expireWatchKey, deleted);
        log.info("[Pay] 支付成功：orderNo={}, userId={}",orderNo,userId);
    }
}
