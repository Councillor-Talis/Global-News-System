package com.gns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    // 回复时传父评论ID，顶级评论不传
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容1-500字")
    private String content;
}