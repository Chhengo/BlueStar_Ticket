package com.ticket.user.config;

import com.ticket.user.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//todo 规范 JWT 认证 自定义的JwtInterceptor优先级低于 Spring Security 的过滤器链
//规范的做法是把 JWT 校验做成 Security 的过滤器，加入到官方过滤链中
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                // 1. 关闭CSRF防护：前后端分离JWT无状态项目，不需要CSRF
                .csrf(csrf -> csrf.disable())

                // 2. 关闭Session：JWT是无状态认证，不创建、不使用Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 接口权限放行规则（核心）
                .authorizeHttpRequests(auth -> auth
                        // 放行注册、登录接口，无需任何认证即可访问
                        .requestMatchers(
                                "/api/user/register",
                                "/api/user/login",
                                "/api/**",
                                "/api/user/hello").permitAll()
                        // 其余所有接口，必须经过认证才能访问
                        .anyRequest().authenticated()
                )
                // 把JWT过滤器加入过滤链，在账号密码认证过滤器之前执行
                // 4. 关闭Spring Security默认的表单登录页（前后端分离不需要）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())

                // 5. 关闭默认的HTTP Basic认证（前后端分离用JWT，不需要）
                .httpBasic(basic -> basic.disable());

        // 构建并返回安全过滤链
        return http.build();
    }
}
