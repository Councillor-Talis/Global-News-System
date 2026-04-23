package com.gns.controller;

import com.gns.common.Result;
import com.gns.dto.LoginDTO;
import com.gns.dto.RegisterDTO;
import com.gns.service.UserService;
import com.gns.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        userService.logout(token);
        return Result.success("退出成功");
    }

}