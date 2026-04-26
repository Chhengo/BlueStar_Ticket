package com.ticket.orders.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    /**
     * 0=待支付  1=已支付  2=已取消(预留，查重走物理删除，此状态保留扩展用)
     */
    private int status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;// 支付成功时写入
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiredTime;// 抢票时写入，+15分钟

}
