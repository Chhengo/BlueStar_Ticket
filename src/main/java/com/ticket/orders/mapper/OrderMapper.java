package com.ticket.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticket.orders.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
