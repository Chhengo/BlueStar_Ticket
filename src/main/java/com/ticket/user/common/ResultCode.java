package com.ticket.user.common;

public class ResultCode {
    public static final ResultCode USERNAME_DUPLICATE = new ResultCode(601,"用户已重复注册");
    public static final ResultCode USER_BANNED = new ResultCode(600,"用户已被封禁");
    //做业务异常处理 401 404
    private Integer code;
    private String msg;

    public ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultCode() {

    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
