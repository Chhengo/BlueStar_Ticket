package com.ticket.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ticket.events.entity.Events;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Mapper
public interface EventsMapper extends BaseMapper<Events> {

    // getLists 用 MyBatis-Plus 内置分页，不需要 XML
    IPage<Events> getLists(Page<Events> page, @Param("status") int status);
}
