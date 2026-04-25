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
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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

    @Value("${gns.upload.path}")
    private String uploadPath;

    @Value("${gns.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    @Override
    public void updateUsername(Long userId, String username) {
        // 检查用户名是否被占用
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .ne(User::getId, userId)  // 排除自己
        );
        if (count > 0) throw new BusinessException("用户名已被占用");

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        userMapper.updateById(user);

        // 同步更新 Redis 中的 token（踢掉旧 token 让用户重新登录）
        // 这里选择不踢，用户名改完继续使用，下次登录生效
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择文件");

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        if (originalName == null) throw new BusinessException("文件名无效");
        String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
            throw new BusinessException("只支持 JPG/PNG/GIF/WEBP 格式");
        }

        // 校验文件大小（最大 2MB）
        if (file.getSize() > 2 * 1024 * 1024) throw new BusinessException("图片不能超过 2MB");

        // 生成唯一文件名
        String fileName = userId + "_" + System.currentTimeMillis() + ext;

        // 保存文件
        try {
            java.io.File dir = new java.io.File(uploadPath);
            if (!dir.exists()) dir.mkdirs();
            file.transferTo(new java.io.File(uploadPath + fileName));
        } catch (Exception e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }

        // 更新数据库头像字段
        String avatarUrl = urlPrefix + fileName;
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);

        return avatarUrl;
    }
}