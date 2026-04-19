package com.ticket.user.dto;

import lombok.Data;
//import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

@Data
public class UserLoginRequest {
//    @NotBlank(message = "用户名不能为空")
    private String username;

//    @NotBlank(message = "密码不能为空")
    private String password;
}
