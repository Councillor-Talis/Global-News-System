package com.gns.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private Long userId;
    private String username;
    private String email;
    private Integer role;
    private String avatar;
    private String token;
}