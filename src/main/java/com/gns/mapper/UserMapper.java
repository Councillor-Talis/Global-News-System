package com.gns.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gns.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 今日新增用户数
    @Select("SELECT COUNT(*) FROM t_user WHERE DATE(created_at) = CURDATE()")
    Long countToday();
}