package com.gns.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gns.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {}