package com.ticket.user.service;

import com.ticket.user.common.Result;
import com.ticket.user.dto.UserLoginRequest;
import com.ticket.user.dto.UserRegisterRequest;
import com.ticket.user.vo.UserLoginResponse;
import com.ticket.user.vo.UserRegisterResponse;

public interface UserService {

    UserRegisterResponse register(UserRegisterRequest req);

    UserLoginResponse login(UserLoginRequest req);
}
