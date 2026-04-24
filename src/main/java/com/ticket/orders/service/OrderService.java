package com.ticket.orders.service;

import com.ticket.orders.entity.GrabRequest;
import com.ticket.orders.entity.Order;
import com.ticket.user.common.Result;

public interface OrderService {
    Result<String> grab(GrabRequest request);

    Result<Order> getOrderByOrderNo(String orderNo);
}
