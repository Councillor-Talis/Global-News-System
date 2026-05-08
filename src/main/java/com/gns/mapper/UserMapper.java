package com.gns.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gns.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 按 id 列表批量查询用户头像，返回 id -> avatar 映射 */
    default Map<Long, String> selectAvatarByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return selectBatchIds(ids).stream()
                .filter(u -> u.getAvatar() != null)
                .collect(Collectors.toMap(User::getId, User::getAvatar));
    }

    // 今日新增用户数
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE(created_at) = CURDATE()")
    Long countToday();
}