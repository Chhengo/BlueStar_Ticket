package com.ticket.user.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.management.loading.MLetContent;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(40,"操作失败"),
    UNKNOWN_PAID_ORDER(41,"请支付后再操作"),
    PAY_OK(201,"支付成功"),
    USER_UNKNOWN_OR_ORDERNO_INVAILD(423, "未知user或非法orderNo"),
    USERNAME_DUPLICATE(410,"用户名已存在"),
    USERNAME_OR_PASSWOR_ERROR(412, "用户名或密码错误"),
    USER_BANNED(403, "账号已被封禁"),
    UNAUTHORIZED(411, "未登录或Token无效"),
    DECR_STOCK_SUCCESS(413, "票已售罄"),
    FAIL_TO_GETTICKET(414, "系统繁忙，请稍后重试");

    private final Integer code;

    private final String msg;
}