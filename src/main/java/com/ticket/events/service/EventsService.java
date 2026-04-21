package com.ticket.events.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticket.events.entity.Events;
import com.ticket.events.entity.TicketType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EventsService {
    IPage<Events> getLists(int page, int size);

    List<TicketType> getDetails(Long eventId);
}
