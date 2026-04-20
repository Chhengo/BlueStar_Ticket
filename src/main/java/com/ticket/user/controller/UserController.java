package com.ticket.user.controller;

import com.ticket.user.common.Result;
import com.ticket.user.dto.UserLoginRequest;
import com.ticket.user.dto.UserRegisterRequest;
import com.ticket.user.service.UserService;
import com.ticket.user.vo.UserLoginResponse;
import com.ticket.user.vo.UserRegisterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    @Autowired
    public UserService userService;
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@RequestBody UserRegisterRequest req){
        log.info("=== register接口被调用 ==="); // 加这行
        return Result.success(userService.register(req));
    }

    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody UserLoginRequest req){
        return Result.success(userService.login(req));
    }

    @GetMapping("/hello")
    public String hello(String num){
        return "hello";
    }
}
