package com.ticket.events.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticket.events.entity.Events;
import com.ticket.events.entity.TicketType;
import com.ticket.events.mapper.EventsMapper;
import com.ticket.events.mapper.TicketsMapper;
import com.ticket.events.service.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventsServiceImpl implements EventsService {

    @Autowired
    private EventsMapper eventsMapper;
    @Autowired
    private TicketsMapper ticketsMapper;
    @Override
    public IPage<Events> getLists(int page, int size) {
        // 只查 status = 1（已发布）的活动
        Page<Events> pageParam = new Page<>(page, size);
        return eventsMapper.getLists(pageParam, 1); // 1 = 已发布
        // 用 MyBatis-Plus 的 Page 对象分页 不需要登录信息，任何人都能看
    }

    @Override
    public List<TicketType> getDetails(Long eventId) {
        // 用 event_id 查 t_ticket_type 表
        // 返回该活动所有票档（A/B/C 三行）
        //获取当前请求id 用户点击来获取具体票档
        //分a b c档位 100 80 60 stock统一为100
        return ticketsMapper.getDetails(eventId);
    }
}
