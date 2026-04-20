package com.ticket.user.config;

import com.ticket.user.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截jwt请求 每次都不用解析token 直接做到了aop 默认拦截所有 只放行指定接口
 * 自动把userid 塞入req controller不再混杂鉴权
 * 把 “鉴权” 这种横切关注点，从业务逻辑中分离出来
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")//所有请求
                .excludePathPatterns(//排除不需要登录的接口
                        "/api/user/register",//注册和登录
                        "/api/user/login"
                );
    }
}

