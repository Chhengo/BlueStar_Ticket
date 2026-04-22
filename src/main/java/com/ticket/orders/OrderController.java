package com.ticket.orders;

import com.ticket.orders.entity.GrabRequest;
import com.ticket.orders.service.OrderService;
import com.ticket.user.common.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
