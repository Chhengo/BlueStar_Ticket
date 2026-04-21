package com.ticket.events.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketType {
    private Long id;
    private Long eventId;
    private String name;
    private int totalStock;
    private BigDecimal price;
    private int remainStock;
    private int perLimit;
}
