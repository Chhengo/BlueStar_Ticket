package com.ticket.pay.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PayRequest {
    private Long userId;
    private String userName;
    private String orderNo;
    private Long eventId;
    private Long ticketId;
    private int status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
}
