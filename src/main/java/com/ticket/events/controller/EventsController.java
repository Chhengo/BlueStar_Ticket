package com.ticket.events.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticket.events.entity.Events;
import com.ticket.events.entity.TicketType;
import com.ticket.events.service.EventsService;
import com.ticket.user.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class EventsController {
    @Autowired
    private EventsService eventsService;
    @GetMapping("/events")
    public Result<IPage<Events>> getEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
//        Long userId = CurrentUserContext.getUserId();
        return Result.success(eventsService.getLists(page, size));
    }
    @GetMapping("/events/{eventId}/ticket-types")
    public Result<List<TicketType>> GetDetails(@PathVariable Long eventId){
        return Result.success(eventsService.getDetails(eventId));
    }
}
