package com.ticket.user.filter;

import com.ticket.user.util.JwtUtil;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 自定义的JwtInterceptor优先级低于 Spring Security 的过滤器链，
 * 规范的做法是把 JWT 校验做成 Security 的过滤器，加入到官方过滤链中，
 * 认证体系更统一、更安全。
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("=== Filter收到请求: {} ===", request.getRequestURI()); // 加这行
        // 1. 从请求头获取Token（标准格式：Bearer 你的token）
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            // 没有Token，直接放行，交给后续Security规则处理
            filterChain.doFilter(request, response);
            return;
        }
        // 2. 解析并校验Token
        String token = authHeader.substring(7);
        try{
            String username = jwtUtil.getUsernameFromToken(token);
            Integer userId = (Integer) jwtUtil.parseToken(token).get("id");

            // 3. 把用户信息存入Security上下文，完成认证
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userId, username, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }catch (Exception e){
            // Token无效，清空上下文，后续Security会自动拦截
            SecurityContextHolder.clearContext();
        }

        // 4. 继续执行过滤链
        filterChain.doFilter(request, response);
    }
}
