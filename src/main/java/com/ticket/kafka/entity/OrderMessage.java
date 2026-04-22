package com.ticket.kafka.entity;

import lombok.Data;

@Data
public class OrderMessage {
    private String userName;
    private Long userId;
    private Long eventId;
    private Long ticketId;
    private String OrderNo;
    //幂等的key 消费者要用这个主键
}
