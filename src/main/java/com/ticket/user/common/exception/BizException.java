package com.ticket.user.common.exception;

import com.ticket.user.common.ResultCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException{
    private final ResultCode resultCode;
    public BizException(ResultCode resultCode){
        //todo extends RuntimeException? 没有这个 super直接报错 为什么
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }
}
