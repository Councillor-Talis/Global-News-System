package com.gns.controller;

import com.gns.common.BusinessException;
import com.gns.common.Result;
import com.gns.dto.LoginDTO;
import com.gns.dto.RegisterDTO;
import com.gns.dto.UpdateUserDTO;
import com.gns.entity.User;
import com.gns.service.UserService;
import com.gns.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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
        userService.logout(authHeader.replace("Bearer ", ""));
        return Result.success("退出成功");
    }

    /** 获取当前用户信息 */
    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userService.getUserById(currentUserId()));
    }

    /** 修改用户名 */
    @PutMapping("/username")
    public Result<?> updateUsername(@Valid @RequestBody UpdateUserDTO dto) {
        userService.updateUsername(currentUserId(), dto.getUsername());
        return Result.success("用户名修改成功");
    }

    /** 上传头像 */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = userService.updateAvatar(currentUserId(), file);
        return Result.success("头像上传成功", url);
    }

    // 临时接口（测试完记得删）
    @GetMapping("/genpass")
    public Result<String> genpass() {
        return Result.success(passwordEncoder.encode("123456"));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
        return (Long) auth.getPrincipal();
    }
}