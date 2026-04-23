package com.gns.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_source_config")
public class SourceConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sourceName;
    private String apiUrl;
    private String apiKey;
    private Long categoryId;
    private Integer isActive;   // 0=禁用 1=启用
    private String cronExpr;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}