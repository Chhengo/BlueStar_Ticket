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

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> Result<T> success(T data){
        return of(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }
    public static <T> Result<T> success(){
        return new Result(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
    }
    public static <T> Result<T> fail(ResultCode rc){
        return of(rc.getCode(), rc.getMsg(), null);
    }
}
