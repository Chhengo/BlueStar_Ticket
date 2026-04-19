package com.ticket.user.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
// 防止业务产生不统一数据 不方便查询日志
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    static ResultCode rc = new ResultCode();
    public static <T> Result<T> success(T data){
        return of(200, "ok", data);
    }
    public static <T> Result<T> fail(T data){
        return of(rc.getCode(), rc.getMsg(), null);
    }
}
