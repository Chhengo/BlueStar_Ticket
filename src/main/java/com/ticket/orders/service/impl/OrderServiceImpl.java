package com.ticket.orders.service.impl;

import com.ticket.kafka.entity.OrderMessage;
import com.ticket.kafka.service.KafkaProducerService;
import com.ticket.orders.entity.GrabRequest;
import com.ticket.orders.entity.Order;
import com.ticket.orders.mapper.OrderMapper;
import com.ticket.orders.service.OrderService;
import com.ticket.redis.service.RateLimiterService;
import com.ticket.redis.service.StockService;
import com.ticket.user.common.Result;
import com.ticket.user.common.ResultCode;
import com.ticket.user.mapper.UserMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private StockService stockService;
    @Autowired
    private KafkaProducerService kafkaProducerService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public Result<String> grab(GrabRequest request) {
        //请求通过限流 扣库存后 再包装为成功信息 如果限流或者库存就失败 返回false
        //限流怎么装？
        boolean allowed = rateLimiterService.isAllowed(
                "grab:event:" + request.getEventId(), 100, 200
        );
        if(!allowed) return Result.fail(ResultCode.FAIL_TO_GETTICKET);

        //扣库存
        boolean deducted = stockService.stockDeductScript(
                request.getEventId(),request.getTicketId());
        if(!deducted){
            return Result.fail(ResultCode.DECR_STOCK_SUCCESS);
        }
        //组装消息发kafka异步下单
        OrderMessage message = new OrderMessage();
        message.setEventId(request.getEventId());
        message.setTicketId(request.getTicketId());
        message.setUserName(request.getUserName());
        message.setUserId(request.getUserId());
        message.setOrderNo(generateOrderNo());//生成唯一订单号

        kafkaProducerService.sendOrderMessage(message);
        //如果用户已经买过该活动的票 就直接拒绝再次买票 已经买过了
        return Result.success("抢票成功，订单处理中");
    }

    @Override
    public Result<Order> getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        order.setUserName(userMapper.getuserNameById(order.getUserId()));
        return Result.success(ResultCode.SUCCESS, order);
    }

    private String generateOrderNo(){
        //时间戳 + 随机数
        return System.currentTimeMillis() + "" + (int)(Math.random() * 10000);
    }
}
