package com.gns.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("t_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long parentId;      // null = 顶级评论，非null = 回复
    private Long userId;
    private String username;
    private String content;
    private Integer likeCount;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 非数据库字段：子回复列表 + 当前用户是否已点赞
    @TableField(exist = false)
    private List<Comment> replies;

    @TableField(exist = false)
    private Boolean liked;
}