package com.ticket.user.vo;

public class UserRegisterResponse {
    private int userId;
    private String username;

    public UserRegisterResponse(int id, String username) {
        this.userId = id;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
