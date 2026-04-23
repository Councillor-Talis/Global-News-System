package com.gns.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gns.common.BusinessException;
import com.gns.dto.LoginDTO;
import com.gns.dto.RegisterDTO;
import com.gns.entity.User;
import com.gns.mapper.ArticleMapper;
import com.gns.mapper.UserMapper;
import com.gns.service.UserService;
import com.gns.utils.JwtUtil;
import com.gns.utils.RedisUtil;
import com.gns.vo.LoginVO;
import com.gns.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Value("${gns.jwt.expire}")
    private Long expire;

    @Override
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) throw new BusinessException("用户名已存在");

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (user == null) throw new BusinessException("用户名或密码错误");
        if (user.getStatus() == 0) throw new BusinessException("账号已被禁用");
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        redisUtil.set("token:access:" + user.getId(), token, expire);

        return LoginVO.builder()
                .userId(user.getId()).username(user.getUsername())
                .email(user.getEmail()).role(user.getRole())
                .avatar(user.getAvatar()).token(token)
                .build();
    }

    @Override
    public void logout(String token) {
        try {
            Long userId = jwtUtil.getUserId(token);
            redisUtil.delete("token:access:" + userId);
        } catch (Exception ignored) {}
    }

    @Override
    public IPage<User> listUsers(Integer page, Integer size) {
        return userMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getId)
        );
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getRole() == 1) throw new BusinessException("不能操作管理员账号");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public StatsVO getStats() {
        return StatsVO.builder()
                .totalArticles(articleMapper.selectCount(null))
                .totalUsers(userMapper.selectCount(null))
                .todayArticles(articleMapper.countToday())
                .totalViews(articleMapper.totalViewCount())
                .build();
    }
}