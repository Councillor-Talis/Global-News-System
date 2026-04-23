package com.gns.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gns.dto.LoginDTO;
import com.gns.dto.RegisterDTO;
import com.gns.entity.User;
import com.gns.vo.LoginVO;
import com.gns.vo.StatsVO;

public interface UserService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    void logout(String token);

    // 管理员功能
    IPage<User> listUsers(Integer page, Integer size);
    void updateUserStatus(Long id, Integer status);
    StatsVO getStats();
}