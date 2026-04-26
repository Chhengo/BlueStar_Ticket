package com.ticket.orders.service.impl;

import com.ticket.orders.entity.Order;
import com.ticket.orders.mapper.OrderMapper;
import com.ticket.redis.config.RedisKeyBuild;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 监听订单过期的redis key信息 传给回补库存服务
 */
@Component
@Slf4j
public class OrderExpireListener extends KeyExpirationEventMessageListener {
    public OrderExpireListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }
    //连接redis客户端 服务启动后有注解来实现redis监听
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderCancelServiceImpl orderCancelService;

    /**
     * 收到 Redis key 过期事件
     *
     * 触发时机：任意Redis key TTL归零自动删除时，Redis向 __keyevent@0__:expired 频道推一条消息
     * 消息内容：过期的key字符串，例如 "ticket:order:1:2:10086:17001234560001"
     *
     * 执行链路：
     *   Step1. 过滤：只处理 ticket:order: 开头的key
     *   Step2. 解析：parseOrderNo() 取最后一段 = orderNo
     *   Step3. 查MySQL：确认 status=0（排除：用户刚支付，pay()里DEL key的消息延迟到达）
     *   Step4. cancelByListener()：Redis库存+1 → MySQL DELETE
     */
    public void onMessage(Message message, byte[] pattern){
        String expiredKey = message.toString();
        log.info("[ExpiredListener] 收到过期时间：key={}", expiredKey);

        //Step1. 过滤：只处理 ticket:order: 开头的key
        if(!RedisKeyBuild.isTicketOrderKey(expiredKey)){
            return;
        }

        //Step2. 解析：parseOrderNo() 取最后一段 = orderNo
        String orderNo = RedisKeyBuild.parseOrderNo(expiredKey);

        // Step3：查MySQL确认订单还在且status=0
        // 场景：用户在第14分59秒支付，pay()里DEL正在监听的key，
        // 但网络延迟导致DEL消息比expired事件晚到 先触发expired事件
        // 再del监听的key 来不及处理 就直接删了
        // 这种极端情况也有 怎么解决? todo
        // 此时MySQL已经是status=1，不能再回补库存
        Order order = orderMapper.getOrderByOrderNo(orderNo);
        if(order == null){
            log.info("[ExpireListener] 订单不存在，可能已支付后被清理，跳过: orderNo={}", orderNo);
            return;
        }
        if(order.getStatus() == 1){
            log.info("[ExpireListener] 订单已支付, orderNo:{}",orderNo);
            return;
        }
        // Step4：执行取消（Redis+1 → MySQL DELETE）
        orderCancelService.cancelByListener(order);
    }
}
