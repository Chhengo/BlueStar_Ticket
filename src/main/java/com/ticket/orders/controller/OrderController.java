package com.ticket.orders.controller;

import com.ticket.orders.entity.GrabRequest;
import com.ticket.orders.entity.Order;
import com.ticket.orders.service.OrderService;
import com.ticket.user.common.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping("/grab")
    public Result<String> grabTicket(@RequestBody GrabRequest request){
        return orderService.grab(request);
    }
    @GetMapping("/pay")
    public Result<Order> getOrder(@RequestBody Order order){
        return orderService.getOrderByOrderNo(order.getOrderNo());
    }
}
