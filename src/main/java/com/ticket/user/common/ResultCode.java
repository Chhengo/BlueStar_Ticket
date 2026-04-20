package com.ticket.user.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    USERNAME_DUPLICATE(400, "用户名已存在"),
    USERNAME_OR_PASSWOR_ERROR(401, "用户名或密码错误"),
    USER_BANNED(403, "账号已被封禁"),
    UNAUTHORIZED(401, "未登录或Token无效");

    private final Integer code;
    private final String msg;
}