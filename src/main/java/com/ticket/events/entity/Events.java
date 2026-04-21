package com.ticket.events.entity;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import lombok.Data;

import java.util.Date;
@Data
public class Events {
    private Long Id;
    private String name;
    private String venue;//场馆
    private Date startTime;
    private Date saleStart;
    private Date saleEnd;
    private int status;


}
