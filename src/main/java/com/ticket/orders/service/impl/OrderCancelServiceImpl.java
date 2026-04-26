package com.ticket.orders.service.impl;

import com.ticket.orders.entity.Order;
import com.ticket.orders.mapper.OrderMapper;
import com.ticket.redis.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
/**
 * 15min内未支付则删除废单
 */
public class OrderCancelServiceImpl {
    //接收redis过期key信息 ttl900s
    //回补redis+1库存 mysql再删除废单 status=0 expireTime<now
    //如果redis加失败 mysql删了 去重时失败 所以要限定这种情况
    //如果mysql删失败 redis+1 定时任务兜底
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StockService stockService;

    /**
     * 监听器专用取消：Redis库存+1 → MySQL物理DELETE
     *
     * 调用方：OrderExpireListener（key过期事件触发）
     * 前提：订单status=0（已在监听器里查过）
     *
     * Step1. Redis库存+1（ticket:stock:{eventId}:{ticketId} +1）
     * Step2. MySQL物理DELETE废单
     * Step3. DELETE失败只记log，库存已经+1了，靠定时任务来兜底清MySQL
     */

    public void cancelByListener(Order order){
        String orderNo = order.getOrderNo();
        log.info("[Cancel-Listener] 开始取消废单：orderNo={}, eventId={}, ticketId={}",
                orderNo, order.getEventId(), order.getTicketId());
        //Step1. Redis库存+1（ticket:stock:{eventId}:{ticketId} +1）
        stockService.stockIncrement(order.getEventId(), order.getTicketId());
        log.info("[Cancel-Linstener] 库存已回补: ticket:stock:{}:{} +1",
                order.getEventId(),order.getTicketId());

        //物理删除废单 避免查重拦截
        int deleted = orderMapper.deleteByOrderNo(orderNo);
        if(deleted >= 0){
            //库存回补 但是mysql删除失败
            //这个废单会在定时任务里5min内会被扫到并delete
            log.warn("[Cancel-Linstener] Mysql delete失败 等待定时任务兜底:orderNo={}", orderNo);
        } else {
            log.info("[Cancel-Linstener] 废单已清除：orderNo={}",orderNo);
        }
    }
    /**
     * 定时任务专用取消：只做MySQL物理DELETE，不碰Redis
     *
     * 调用方：OrderCleanupScheduler（每5分钟扫描）
     * 前提：这条废单能被扫到，说明监听器已经执行过Redis+1，库存已回补
     * 如果再+1，ticket:stock的值会比实际可售库存多1，导致多卖票
     **/
    public void cancelByScheduled(String orderNo){
        log.info("[Cancel-Scheduler] 定时兜底清理废单：orderNo={}", orderNo);
        int deleted = orderMapper.deleteByOrderNo(orderNo);
        if(deleted == 0){
            log.warn("[Cancel-Scheduler] 废单未被删除 后续内容待开发:orderNo={}",orderNo);
        }else{
            log.info("[Cancel-Scheduler] 废单已清除");
        }
    }
}
