package com.gns.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名3-20位")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码6-20位")
    private String password;
    @Email(message = "邮箱格式错误")
    private String email;
}