package com.ticket.user.vo;

public class UserLoginResponse {
    private String token;
    private String username;
    private Long expiretime;

    public UserLoginResponse(String token,Long expiretime) {
        this.token = token;
        this.expiretime = expiretime;
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

    public Long getexpiretime() {
        return expiretime;
    }

    public void setexpiretime(Long expiretime) {
        this.expiretime = expiretime;
    }
}
