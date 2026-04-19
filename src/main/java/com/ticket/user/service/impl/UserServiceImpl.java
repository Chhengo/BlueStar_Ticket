package com.ticket.user.service.impl;

import com.ticket.user.common.ResultCode;
import com.ticket.user.common.exception.BizException;
import com.ticket.user.dto.UserLoginRequest;
import com.ticket.user.dto.UserRegisterRequest;
import com.ticket.user.entity.UserEntity;
import com.ticket.user.mapper.UserMapper;
import com.ticket.user.service.UserService;
import com.ticket.user.util.JwtUtil;
import com.ticket.user.vo.UserLoginResponse;
import com.ticket.user.vo.UserRegisterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    private JwtUtil jwtUtil;
//    private PasswordEncoder encoder;// todo BCryptPasswordEncoder ? 无法引用 切换内容

    public UserRegisterResponse register(UserRegisterRequest req) {
        //返回注册成功的注册响应
        //1.查重
        if(userMapper.findByName(req.getUsername() != null)){
            throw new BizException(ResultCode.USERNAME_DUPLICATE);
        }
        //2.构建账户
        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
//        user.setPasswordHash(req.getPassword());
        user.setPhone(req.getPhone());
        user.setRealName(req.getRealName());
        user.setStatus(1);
        userMapper.insert(user);

        return new UserRegisterResponse(user.getId(), user.getUsername());
    }

    public UserLoginResponse login(UserLoginRequest req) {
        //1.查用户
        UserEntity user = userMapper.findByUsername(req.getUsername());
        //todo 优化
//        if(user == null || !encoder.matches(req.getPassword(), user.getPasswordHash()));{
//            throw new BizException(ResultCode.USERNAME_OR_PASSWOR_ERROR);
//        }
        //2.封号校验
        if(user.getStatus() == 0){
            throw new BizException(ResultCode.USER_BANNED);
        }
        //3.生成token凭证
        String token = jwtUtil.generate(user.getId(), user.getUsername());
        return new UserLoginResponse(token, System.currentTimeMillis());
    }
}
