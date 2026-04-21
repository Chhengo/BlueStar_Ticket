package com.ticket.events.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticket.events.entity.Events;
import com.ticket.events.entity.TicketType;
import com.ticket.events.service.EventsService;
import com.ticket.user.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class EventsController {
    @Autowired
    private EventsService eventsService;
    @GetMapping("/events/list")
    public Result<IPage<Events>> getEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
//        Long userId = CurrentUserContext.getUserId();
        return Result.success(eventsService.getLists(page, size));
    }
    @PostMapping("/events/insert")
    public Result<String> createEvent(@RequestBody Events events){
        eventsService.createEvent(events);
        return Result.success("活动已添加");
    }

    @PutMapping("/events/update/{id}")
    public Result<String> updateEvents(@RequestBody Events events){
        eventsService.updateEvent(events);
        return Result.success("已修改");
    }

    @DeleteMapping ("/events/delete/{id}")
    public Result<String> deleteEvent(@PathVariable Long id){
        eventsService.deleteEvent(id);
        // 🔥 错误写法："id为:${id}"（EL表达式不生效）
        return Result.success("id为" + id + "的活动已删除");
    }

    @GetMapping("/events/{eventId}/ticket-types")
    public Result<List<TicketType>> GetDetails(@PathVariable Long eventId){
        return Result.success(eventsService.getDetails(eventId));
    }
}
