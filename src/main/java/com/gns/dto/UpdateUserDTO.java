package com.gns.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @Size(min = 3, max = 20, message = "用户名3-20位")
    private String username;
}