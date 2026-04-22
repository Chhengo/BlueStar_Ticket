package com.ticket.kafka.service;

import com.ticket.kafka.config.KafkaTopicConfig;
import com.ticket.kafka.entity.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 生产者发消息
 */
@Service
public class KafkaProducerService {
    //生成orderNo 保证扣库存幂等性
    @Autowired
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;

    //发送消息
    public void sendOrderMessage(OrderMessage message){
        kafkaTemplate.send(
                KafkaTopicConfig.ORDER_TOPIC,
                String.valueOf(message.getUserId()),
                message
        );
    }
}
