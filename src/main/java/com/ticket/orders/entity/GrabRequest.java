package com.ticket.orders.entity;

import lombok.Data;

@Data
public class GrabRequest {
    private String userName;
    private Long userId;
    private Long eventId;
    private Long ticketId;
}
