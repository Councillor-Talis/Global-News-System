package com.gns.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String summary;
    private String content;
    private String source;
    private String sourceUrl;
    private Long categoryId;
    private String coverImg;
    private LocalDateTime pubTime;
    private Integer status;
}