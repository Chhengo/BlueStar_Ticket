package com.ticket.kafka.service;

import com.ticket.kafka.config.KafkaTopicConfig;
import com.ticket.kafka.entity.OrderMessage;
import com.ticket.orders.entity.Order;
import com.ticket.orders.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消费者异步落库
 */
@Service
public class KafkaConsumerService {
    @Autowired
    private OrderMapper orderMapper;
    @KafkaListener(topics = KafkaTopicConfig.ORDER_TOPIC, groupId = "order-group")
    public void consume(OrderMessage message){
        //幂等校验 orderNo
        if(orderMapper.existsByOrderNo(message.getOrderNo())){
            return;
        }

        //校验同一用户同一活动不能重复买票
        if(orderMapper.existsByUserAndEvent(message.getUserId(), message.getEventId())){
            return; //已经购买了 直接丢掉
        }

        Order order = new Order();
        order.setOrderNo(message.getOrderNo());
        order.setEventId(message.getEventId());
        order.setUserId(message.getUserId());
        order.setUserName(message.getUserName());
        order.setTicketId(message.getTicketId());
        order.setStatus("PAID");
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insertOrder(order);
    }
}
