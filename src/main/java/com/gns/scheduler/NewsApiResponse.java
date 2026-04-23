package com.gns.scheduler;

import lombok.Data;
import java.util.List;

/** NewsAPI 响应结构映射 */
@Data
public class NewsApiResponse {

    private String status;
    private Integer totalResults;
    private List<NewsArticle> articles;

    @Data
    public static class NewsArticle {
        private Source source;
        private String author;
        private String title;
        private String description;  // 摘要
        private String url;          // 原文链接
        private String urlToImage;   // 封面图
        private String publishedAt;  // 发布时间（ISO格式）
        private String content;      // 正文（免费版截断）

        @Data
        public static class Source {
            private String id;
            private String name;     // BBC News / Reuters 等
        }
    }
}