package com.gns.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * ES 文档映射类，对应 MySQL 的 t_article 表
 * 一篇文章在 ES 里存一份，用于全文搜索
 */
@Data
@Document(indexName = "gns_articles")  // ES 索引名
@Setting(settingPath = "es-settings.json")  // 索引配置（分词器等）
public class ArticleDocument {

    @Id
    private Long id;

    // ik_max_word：索引时细粒度分词；ik_smart：搜索时智能分词
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Keyword)  // keyword 不分词，用于精确过滤
    private String source;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String coverImg;

    @Field(type = FieldType.Keyword)
    private String sourceUrl;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Keyword)
    private String pubTime;
}