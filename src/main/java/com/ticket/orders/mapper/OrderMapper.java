package com.ticket.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticket.orders.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COUNT(1) FROM ticket.t_order WHERE order_no = #{orderNo}")
    int countByOrderNo(String orderNo);
    @Select("SELECT count(1) from ticket.t_order where user_id = #{userId} AND event_id = #{eventId}")
    int countByUserAndEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);
    default boolean existsByOrderNo(String orderNo){
        return countByOrderNo(orderNo) > 0;
    }

    default boolean existsByUserAndEvent(Long userId, Long eventId){
        return countByUserAndEvent(userId, eventId) > 0;
    };

    void insertOrder(Order order);

    Order getOrderByOrderNo(String orderNo);
    @Update("update ticket.t_order SET status = 1, pay_time = #{payTime} " +
            "where order_no = #{orderNo} and status = 0")
    int payOrder(String orderNo, LocalDateTime now);
    @Delete("delete from ticket.t_order where order_no = #{orderNo}")
    int deleteByOrderNo(String orderNo);

    @Select("select order_no FROM ticket.t_order where status = 0 and expired_time < NOW()")
    List<String> selectExpiredOrderNos();
}
