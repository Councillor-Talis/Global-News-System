package com.gns.vo;

import lombok.Builder;
import lombok.Data;

/** 数据概览统计 */
@Data
@Builder
public class StatsVO {
    private Long totalArticles;   // 文章总数
    private Long totalUsers;      // 用户总数
    private Long todayArticles;   // 今日新增文章
    private Long totalViews;      // 总阅读量
}