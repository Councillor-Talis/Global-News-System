package com.gns.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Integer sort;
}