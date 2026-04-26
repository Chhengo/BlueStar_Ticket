package com.ticket.pay.controller;

import com.ticket.pay.Entity.PayRequest;
import com.ticket.pay.service.PayService;
import com.ticket.user.common.Result;
import com.ticket.user.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/api")
@Slf4j
@RequiredArgsConstructor
public class PayController {
    /**
     * P1 · POST /orders/pay
     * Body: { "orderNo": "...", "userId": 123 }
     *
     * 生产环境 userId 从 JWT token 解析，此处简化为请求体参数
     */
    @Autowired
    private PayService payService;
    @PostMapping("/pay")
    public Result<Map<String, Object>> pay(@RequestBody PayRequest req){
        try{
            payService.pay(req.getOrderNo(), req.getUserId());
            return Result.success(ResultCode.PAY_OK, null);
        } catch (IllegalArgumentException e){
            log.warn("Pay 参数异常：{}", e.getMessage());
            //参数错误 订单不存在或者越权
            return Result.fail(ResultCode.USER_UNKNOWN_OR_ORDERNO_INVAILD);
       } catch (IllegalStateException e){
            log.warn("Pay 状态异常：{}", e.getMessage());
            //超时 重复支付 并发冲突
            return Result.fail(ResultCode.UNKNOWN_PAID_ORDER);
        }
    }
}
