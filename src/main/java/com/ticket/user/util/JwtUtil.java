package com.ticket.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component //?啥意思来着 注册为springbean 才能在service里注入
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    //生成key对象
    private Key getSignKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成token
     * @param id
     * @param username
     * @return
     */
    public String generate(int id, String username) {
        Map<String, Object> map = new HashMap<>();
        //放入非敏感信息
        map.put("id",id);
        map.put("username",username);

        return Jwts.builder()
                .setClaims(map)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.ES256)
                .compact();//设置payload
    }

    /**
     * 解析token
     * @param token
     * @return
     */
    public Claims parseToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取用户名
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token){
        return parseToken(token).getSubject();
    }

    /**
     * 校验token是否过期
     * @param token
     * @return
     */
    public Boolean isTokenExpired(String token){
        return parseToken(token).getExpiration().before(new Date());
    }
}


