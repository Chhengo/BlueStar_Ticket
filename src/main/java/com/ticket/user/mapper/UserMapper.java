package com.ticket.user.mapper;

import com.ticket.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
@Mapper
public interface UserMapper {

    boolean findByName(boolean b);

    void insert(UserEntity user);

    UserEntity findByUsername(String username);
}
