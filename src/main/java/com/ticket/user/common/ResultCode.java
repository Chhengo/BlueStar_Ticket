package com.ticket.user.common;

public class ResultCode {
    public static final ResultCode USERNAME_DUPLICATE = new ResultCode(601,"用户已重复注册");
    public static final ResultCode USER_BANNED = new ResultCode(600,"用户已被封禁");
    public static final ResultCode USERNAME_OR_PASSWOR_ERROR = new ResultCode(602, "用户密码或者用户名错误");
    public static final ResultCode UNAUTHORIZED = new ResultCode(603, "未登录或Token无效");

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
