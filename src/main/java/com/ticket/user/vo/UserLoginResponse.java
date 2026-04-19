package com.ticket.user.vo;

public class UserLoginResponse {
    private String token;
    private String username;
    private Long time;

    public UserLoginResponse(String token,Long time) {
        this.token = token;
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
