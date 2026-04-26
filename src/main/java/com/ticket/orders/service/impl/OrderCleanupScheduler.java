package com.ticket.orders.service.impl;

import com.ticket.orders.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OrderCleanupScheduler {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderCancelServiceImpl orderCancelService;
    /**
     * 每5分钟扫一次 orders 表中的超时废单
     *
     * 扫描条件：status=0 AND expire_time < NOW()
     * 这类废单的来源：监听器已经执行 Redis+1，但 MySQL DELETE 失败，废单残留
     *
     * 执行动作：只做 MySQL DELETE，不碰 Redis
     * 原因：Redis+1 已经在监听器里做过了，若定时任务再+1
     *       ticket:stock:{eventId}:{ticketId} 的值 = 真实库存+1，其他用户会多买一张票
     *
     * 失败处理：DELETE失败只记log，下次5分钟后继续扫，直到成功为止
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5分钟？
    public void cleanExpiredOrder(){
        List<String> list = orderMapper.selectExpiredOrderNos();
        if(list == null || list.isEmpty()){
            log.debug("[Scheduler] 无超时费废单；跳过");
            return;
        }
        log.info("[Scheduler] 扫到超时废单 {} 条：开始清理", list.size());
        for(String orderNo : list){
            orderCancelService.cancelByScheduled(orderNo);
        }
    }
}
