package com.ticket.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticket.events.entity.Events;
import com.ticket.events.entity.TicketType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper
public interface TicketsMapper extends BaseMapper<Events> {
     List<TicketType> getDetails(@Param("eventId") Long eventId);
}
