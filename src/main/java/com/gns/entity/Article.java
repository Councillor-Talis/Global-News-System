package com.gns.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String source;
    private String sourceUrl;
    private Long categoryId;
    private String coverImg;
    private LocalDateTime pubTime;
    private Integer viewCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}