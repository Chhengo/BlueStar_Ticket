package com.ticket.orders.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Order {
    private Long userId;
    private String userName;
    private String orderNo;
    private Long eventId;
    private Long ticketId;
    private int status;
    private LocalDateTime createTime;

}
