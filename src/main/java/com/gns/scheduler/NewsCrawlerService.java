package com.gns.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gns.entity.Article;
import com.gns.entity.SourceConfig;
import com.gns.mapper.ArticleMapper;
import com.gns.mapper.SourceConfigMapper;
import com.gns.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCrawlerService {

    private final SourceConfigMapper sourceConfigMapper;
    private final ArticleMapper articleMapper;
    private final RedisUtil redisUtil;
    private final RestTemplate restTemplate;
    private final ArticleContentFetcher contentFetcher;  // 注入正文抓取器
    private final ArticleCategorizer categorizer;

    @Value("${gns.newsapi.key}")
    private String apiKey;

    @Value("${gns.newsapi.base-url}")
    private String baseUrl;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void scheduledCrawl() {
        log.info("========== 定时采集开始 ==========");
        crawlAll();
        log.info("========== 定时采集完成 ==========");
    }

    public String manualCrawl() {
        log.info("===== 手动触发采集 =====");
        int count = crawlAll();
        return "采集完成，新增文章 " + count + " 篇";
    }

    private int crawlAll() {
        List<SourceConfig> configs = sourceConfigMapper.selectList(
                new LambdaQueryWrapper<SourceConfig>().eq(SourceConfig::getIsActive, 1)
        );
        if (configs.isEmpty()) {
            log.warn("没有启用的新闻来源配置");
            return 0;
        }

        int totalInserted = 0;
        for (SourceConfig config : configs) {
            try {
                int inserted = crawlOne(config);
                totalInserted += inserted;
                log.info("来源[{}] 新增 {} 篇文章", config.getSourceName(), inserted);
            } catch (Exception e) {
                log.error("来源[{}] 采集失败: {}", config.getSourceName(), e.getMessage());
            }
        }

        if (totalInserted > 0) {
            redisUtil.deleteByPrefix("news:list:");
            redisUtil.delete("news:hot");
            log.info("已清除缓存，本次共新增 {} 篇", totalInserted);
        }
        return totalInserted;
    }

    private int crawlOne(SourceConfig config) {
        String url = buildUrl(config);
        log.info("请求 NewsAPI: {}", url);

        ResponseEntity<NewsApiResponse> response =
                restTemplate.getForEntity(url, NewsApiResponse.class);

        if (response.getBody() == null ||
                !"ok".equals(response.getBody().getStatus()) ||
                response.getBody().getArticles() == null) {
            log.warn("来源[{}] NewsAPI 返回异常: {}", config.getSourceName(),
                    response.getBody() != null ? response.getBody().getStatus() : "null");
            return 0;
        }

        List<NewsApiResponse.NewsArticle> articles = response.getBody().getArticles();
        int inserted = 0;

        for (NewsApiResponse.NewsArticle item : articles) {
            // 跳过无效条目
            if (item.getTitle() == null || "[Removed]".equals(item.getTitle())) continue;
            if (item.getUrl() == null) continue;

            // 去重
            Long exists = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>().eq(Article::getSourceUrl, item.getUrl())
            );
            if (exists > 0) continue;

            // 抓取原文正文
            log.info("正在抓取原文: {}", item.getUrl());
            String fullContent = contentFetcher.fetchContent(item.getUrl());

            // 如果抓取失败，用 NewsAPI 自带的摘要兜底
            String content = buildContent(item, fullContent);

            Article article = new Article();
            article.setTitle(item.getTitle());
            article.setSummary(item.getDescription());
            article.setContent(content);
            article.setSource(config.getSourceName());
            article.setSourceUrl(item.getUrl());
// 智能分类：关键词匹配优先，匹配不到才用来源配置的分类
            Long smartCategory = categorizer.categorize(
                    item.getTitle(),
                    item.getDescription(),
                    config.getCategoryId()
            );
            article.setCategoryId(smartCategory);
            article.setCoverImg(item.getUrlToImage());
            article.setPubTime(parseTime(item.getPublishedAt()));
            article.setViewCount(0);
            article.setStatus(1);

            articleMapper.insert(article);
            inserted++;
        }

        return inserted;
    }

    /**
     * 构造正文 HTML
     * 优先使用抓取到的完整正文，失败则用 NewsAPI 摘要兜底
     */
    private String buildContent(NewsApiResponse.NewsArticle item, String fullContent) {
        StringBuilder sb = new StringBuilder();

        if (fullContent != null && fullContent.length() > 100) {
            // 抓取成功：使用完整正文
            sb.append(fullContent);
        } else {
            // 抓取失败：用摘要兜底
            if (item.getDescription() != null) {
                sb.append("<p>").append(item.getDescription()).append("</p>");
            }
            if (item.getContent() != null) {
                String c = item.getContent().replaceAll("\\[\\+\\d+ chars\\]", "").trim();
                if (!c.isEmpty()) sb.append("<p>").append(c).append("</p>");
            }
        }

        // 末尾始终附上原文链接
        if (item.getUrl() != null) {
            sb.append("<p style='margin-top:16px;padding-top:12px;border-top:1px solid #e2e8f0'>")
                    .append("<a href='").append(item.getUrl())
                    .append("' target='_blank' style='color:#3b82f6'>阅读原文 →</a></p>");
        }

        return sb.toString();
    }

    private String buildUrl(SourceConfig config) {
        if (config.getApiUrl() != null && !config.getApiUrl().isEmpty()) {
            String u = config.getApiUrl();
            String sep = u.contains("?") ? "&" : "?";
            return u + sep + "apiKey=" + apiKey + "&pageSize=20&language=en";
        }
        String sourceName = config.getSourceName()
                .toLowerCase().replace(" ", "-").replace(".", "");
        return baseUrl + "/top-headlines?sources=" + sourceName
                + "&apiKey=" + apiKey + "&pageSize=20";
    }

    private LocalDateTime parseTime(String publishedAt) {
        if (publishedAt == null) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(publishedAt).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}