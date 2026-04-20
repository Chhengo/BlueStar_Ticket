package com.ticket.user.interceptor;

import com.ticket.user.common.ResultCode;
import com.ticket.user.common.exception.BizException;
import com.ticket.user.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用于拦截未能携带token的请求
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    public boolean prehandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception{
        // 1. 从请求头中获取 Token (标准做法是放在 Authorization 头里，格式为 "Bearer xxx")
        String authHeader = req.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            log.warn("请求未携带 Token 或格式错误：URI={}", req.getRequestURI());
            throw new BizException(ResultCode.UNAUTHORIZED);
        }

        //提取真正token 去掉 Bearer
        String token = authHeader.substring(7);

        try{
            //解析并验证token
            Claims claims = jwtUtil.parseToken(token);

            //把用户信息存入Request上下文
            Integer userId = (Integer) claims.get("id");
            String username = claims.getSubject();
            req.setAttribute("userId",userId);
            req.setAttribute("username",username);

            log.info("Token验证通过：userId={}, username={}", userId , username);
            return true;
        } catch (Exception e){
            log.warn("token验证失败:{}", token);
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
    }
}
