package com.ticket.orders.service;

import com.ticket.orders.entity.GrabRequest;
import com.ticket.user.common.Result;

public interface OrderService {
    Result<String> grab(GrabRequest request);
}
