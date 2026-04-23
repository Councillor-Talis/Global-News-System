package com.gns.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * 从原文 URL 抓取文章正文
 * 支持 BBC / Reuters / AP News 等主流媒体
 */
@Slf4j
@Component
public class ArticleContentFetcher {

    // 超时时间（毫秒）
    private static final int TIMEOUT = 8000;

    /**
     * 抓取指定 URL 的正文内容
     * @return HTML 格式的正文，失败时返回 null
     */
    public String fetchContent(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .get();

            // 按来源用不同的选择器提取正文
            String content = null;

            if (url.contains("bbc.com") || url.contains("bbc.co.uk")) {
                content = extractBBC(doc);
            } else if (url.contains("reuters.com")) {
                content = extractReuters(doc);
            } else if (url.contains("apnews.com")) {
                content = extractAP(doc);
            } else {
                content = extractGeneric(doc);
            }

            // 如果专用解析失败，用通用方法兜底
            if (content == null || content.trim().isEmpty()) {
                content = extractGeneric(doc);
            }

            return content;

        } catch (Exception e) {
            log.warn("抓取原文失败 [{}]: {}", url, e.getMessage());
            return null;
        }
    }

    /** BBC News 正文提取 */
    private String extractBBC(Document doc) {
        StringBuilder sb = new StringBuilder();
        // BBC 正文在 article 标签内的 p 标签
        Elements articles = doc.select("article");
        if (!articles.isEmpty()) {
            Elements paras = articles.first().select("p");
            for (Element p : paras) {
                String text = p.text().trim();
                if (text.length() > 30) {  // 过滤太短的段落（广告/版权等）
                    sb.append("<p>").append(text).append("</p>");
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /** Reuters 正文提取 */
    private String extractReuters(Document doc) {
        StringBuilder sb = new StringBuilder();
        // Reuters 正文容器
        Elements paras = doc.select("[class*='article-body'] p, " +
                "[class*='ArticleBody'] p, " +
                "[data-testid='paragraph'] p");
        if (paras.isEmpty()) {
            paras = doc.select("article p");
        }
        for (Element p : paras) {
            String text = p.text().trim();
            if (text.length() > 30) {
                sb.append("<p>").append(text).append("</p>");
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /** AP News 正文提取 */
    private String extractAP(Document doc) {
        StringBuilder sb = new StringBuilder();
        Elements paras = doc.select(".RichTextStoryBody p, " +
                "[class*='story-body'] p, " +
                "div.Article p");
        if (paras.isEmpty()) {
            paras = doc.select("article p");
        }
        for (Element p : paras) {
            String text = p.text().trim();
            if (text.length() > 30) {
                sb.append("<p>").append(text).append("</p>");
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 通用正文提取（适用于其他来源）
     * 找页面中段落最多的容器，提取其中的 p 标签
     */
    private String extractGeneric(Document doc) {
        // 移除干扰元素
        doc.select("nav, header, footer, aside, script, style, " +
                "[class*='ad'], [class*='menu'], [class*='social'], " +
                "[class*='recommend'], [class*='related']").remove();

        // 找段落数最多的容器
        Elements candidates = doc.select("article, main, " +
                "[class*='content'], [class*='body'], [class*='story']");

        Element best = null;
        int maxP = 0;
        for (Element el : candidates) {
            int count = el.select("p").size();
            if (count > maxP) {
                maxP = count;
                best = el;
            }
        }

        // 找不到容器就直接取 body 下所有 p
        Elements paras = (best != null) ? best.select("p") : doc.select("body p");

        StringBuilder sb = new StringBuilder();
        for (Element p : paras) {
            String text = p.text().trim();
            if (text.length() > 40) {
                sb.append("<p>").append(text).append("</p>");
            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }
}