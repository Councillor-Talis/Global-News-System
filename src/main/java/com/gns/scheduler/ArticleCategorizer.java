package com.gns.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 文章智能分类器 v2
 * 策略：多关键词加权匹配，得分最高的分类胜出
 */
@Slf4j
@Component
public class ArticleCategorizer {

    private static final long CAT_POLITICS   = 2L;
    private static final long CAT_MILITARY   = 3L;
    private static final long CAT_ECONOMY    = 4L;
    private static final long CAT_TECHNOLOGY = 5L;
    private static final long CAT_HEALTH     = 6L;

    /**
     * 关键词权重表
     * 权重 3 = 强特征词（几乎只出现在该分类）
     * 权重 2 = 中特征词
     * 权重 1 = 弱特征词（可能跨分类出现）
     */
    private static final Map<Long, Map<String, Integer>> WEIGHTED_KEYWORDS = new LinkedHashMap<>();

    static {
        // ===== 军事 / 冲突 / 灾害 =====
        Map<String, Integer> military = new LinkedHashMap<>();
        // 强特征（权重3）
        military.put("missile strike", 3);
        military.put("air strike", 3);
        military.put("military operation", 3);
        military.put("troops deployed", 3);
        military.put("nuclear weapon", 3);
        military.put("ceasefire", 3);
        military.put("artillery", 3);
        military.put("warship", 3);
        military.put("fighter jet", 3);
        military.put("drone attack", 3);
        military.put("suicide bomber", 3);
        military.put("improvised explosive", 3);
        // 自然灾害（权重3）
        military.put("earthquake", 3);
        military.put("tsunami", 3);
        military.put("hurricane", 3);
        military.put("typhoon", 3);
        military.put("wildfire", 3);
        military.put("volcano", 3);
        military.put("magnitude", 3);    // 地震震级
        military.put("richter", 3);
        military.put("evacuation order", 3);
        // 中特征（权重2）
        military.put("military", 2);
        military.put("combat", 2);
        military.put("battlefield", 2);
        military.put("invasion", 2);
        military.put("occupation", 2);
        military.put("nato", 2);
        military.put("pentagon", 2);
        military.put("war crime", 2);
        military.put("refugee", 2);
        military.put("casualties", 2);
        military.put("airstrike", 2);
        military.put("terror", 2);
        military.put("hostage", 2);
        military.put("disaster", 2);
        military.put("flood", 2);
        military.put("cyclone", 2);
        military.put("landslide", 2);
        // 弱特征（权重1）
        military.put("war", 1);
        military.put("army", 1);
        military.put("soldier", 1);
        military.put("bomb", 1);
        military.put("attack", 1);
        military.put("conflict", 1);
        military.put("defense", 1);
        military.put("weapon", 1);
        WEIGHTED_KEYWORDS.put(CAT_MILITARY, military);

        // ===== 健康 =====
        Map<String, Integer> health = new LinkedHashMap<>();
        military.put("killed in", 2);
        // 强特征（权重3）
        health.put("clinical trial", 3);
        health.put("fda approved", 3);
        health.put("vaccine efficacy", 3);
        health.put("pandemic", 3);
        health.put("outbreak", 3);
        health.put("world health organization", 3);
        health.put("cancer treatment", 3);
        health.put("mental health", 3);
        health.put("public health", 3);
        health.put("drug overdose", 3);
        health.put("opioid", 3);
        health.put("alzheimer", 3);
        health.put("dementia", 3);
        // 中特征（权重2）
        health.put("disease", 2);
        health.put("virus", 2);
        health.put("cancer", 2);
        health.put("vaccine", 2);
        health.put("hospital", 2);
        health.put("medicine", 2);
        health.put("treatment", 2);
        health.put("patient", 2);
        health.put("surgery", 2);
        health.put("diabetes", 2);
        health.put("obesity", 2);
        health.put("epidemic", 2);
        health.put("infection", 2);
        health.put("symptom", 2);
        health.put("pharmaceutical", 2);
        health.put("biotech", 2);
        health.put("who ", 2);
        health.put("cdc", 2);
        health.put("nih", 2);
        // 弱特征（权重1）
        health.put("health", 1);
        health.put("doctor", 1);
        health.put("drug", 1);
        health.put("medical", 1);
        health.put("therapy", 1);
        health.put("research", 1);
        WEIGHTED_KEYWORDS.put(CAT_HEALTH, health);

        // ===== 科技 =====
        Map<String, Integer> tech = new LinkedHashMap<>();
        // 强特征（权重3）
        tech.put("artificial intelligence", 3);
        tech.put("machine learning", 3);
        tech.put("large language model", 3);
        tech.put("chatgpt", 3);
        tech.put("openai", 3);
        tech.put("generative ai", 3);
        tech.put("semiconductor", 3);
        tech.put("silicon valley", 3);
        tech.put("spacex launch", 3);
        tech.put("cybersecurity breach", 3);
        tech.put("data breach", 3);
        tech.put("quantum computing", 3);
        tech.put("self-driving", 3);
        tech.put("autonomous vehicle", 3);
        tech.put("blockchain", 3);
        tech.put("cryptocurrency", 3);
        // 中特征（权重2）
        tech.put("google", 2);
        tech.put("apple", 2);
        tech.put("microsoft", 2);
        tech.put("amazon", 2);
        tech.put("meta", 2);
        tech.put("tesla", 2);
        tech.put("spacex", 2);
        tech.put("nvidia", 2);
        tech.put("software", 2);
        tech.put("startup", 2);
        tech.put("algorithm", 2);
        tech.put("cybersecurity", 2);
        tech.put("smartphone", 2);
        tech.put("electric vehicle", 2);
        tech.put("5g", 2);
        tech.put("chip", 2);
        tech.put("robot", 2);
        tech.put("iphone", 2);
        tech.put("android", 2);
        tech.put("hack", 2);
        // 弱特征（权重1）
        tech.put("technology", 1);
        tech.put("tech", 1);
        tech.put("digital", 1);
        tech.put("internet", 1);
        tech.put("cloud", 1);
        tech.put("data", 1);
        tech.put("ai ", 1);
        WEIGHTED_KEYWORDS.put(CAT_TECHNOLOGY, tech);

        // ===== 经济 =====
        Map<String, Integer> economy = new LinkedHashMap<>();
        // 强特征（权重3）
        economy.put("federal reserve", 3);
        economy.put("interest rate", 3);
        economy.put("stock market", 3);
        economy.put("gdp growth", 3);
        economy.put("inflation rate", 3);
        economy.put("central bank", 3);
        economy.put("trade deficit", 3);
        economy.put("trade surplus", 3);
        economy.put("ipo", 3);
        economy.put("merger acquisition", 3);
        economy.put("layoffs", 3);
        economy.put("supply chain", 3);
        economy.put("oil price", 3);
        economy.put("opec", 3);
        economy.put("imf", 3);
        economy.put("world bank", 3);
        // 中特征（权重2）
        economy.put("economy", 2);
        economy.put("economic", 2);
        economy.put("market", 2);
        economy.put("trade", 2);
        economy.put("inflation", 2);
        economy.put("recession", 2);
        economy.put("investment", 2);
        economy.put("finance", 2);
        economy.put("revenue", 2);
        economy.put("profit", 2);
        economy.put("tariff", 2);
        economy.put("employment", 2);
        economy.put("unemployment", 2);
        economy.put("tax", 2);
        economy.put("budget", 2);
        economy.put("debt", 2);
        economy.put("currency", 2);
        economy.put("dollar", 2);
        economy.put("euro", 2);
        economy.put("bond", 2);
        economy.put("hedge fund", 2);
        // 弱特征（权重1）
        economy.put("bank", 1);
        economy.put("growth", 1);
        economy.put("export", 1);
        economy.put("import", 1);
        economy.put("billion", 1);
        economy.put("trillion", 1);
        WEIGHTED_KEYWORDS.put(CAT_ECONOMY, economy);

        // ===== 政治 =====
        Map<String, Integer> politics = new LinkedHashMap<>();
        // 强特征（权重3）
        politics.put("presidential election", 3);
        politics.put("general election", 3);
        politics.put("prime minister", 3);
        politics.put("secretary of state", 3);
        politics.put("united nations", 3);
        politics.put("security council", 3);
        politics.put("white house", 3);
        politics.put("congress", 3);
        politics.put("senate", 3);
        politics.put("supreme court", 3);
        politics.put("diplomatic", 3);
        politics.put("sanction", 3);
        politics.put("bilateral", 3);
        politics.put("geopolitical", 3);
        // 中特征（权重2）
        politics.put("president", 2);
        politics.put("government", 2);
        politics.put("election", 2);
        politics.put("vote", 2);
        politics.put("policy", 2);
        politics.put("legislation", 2);
        politics.put("parliament", 2);
        politics.put("democrat", 2);
        politics.put("republican", 2);
        politics.put("eu ", 2);
        politics.put("summit", 2);
        politics.put("treaty", 2);
        politics.put("minister", 2);
        politics.put("ambassador", 2);
        politics.put("biden", 2);
        politics.put("trump", 2);
        politics.put("xi jinping", 2);
        politics.put("putin", 2);
        politics.put("zelensky", 2);
        // 弱特征（权重1）
        politics.put("political", 1);
        politics.put("law", 1);
        politics.put("court", 1);
        politics.put("protest", 1);
        politics.put("opposition", 1);
        WEIGHTED_KEYWORDS.put(CAT_POLITICS, politics);
    }

    /**
     * 智能分类：加权评分，得分最高的分类胜出
     */
    public Long categorize(String title, String description, Long defaultCategoryId) {
        String text = buildSearchText(title, description);
        if (text.isEmpty()) return defaultCategoryId;

        Map<Long, Integer> scores = new LinkedHashMap<>();
        scores.put(CAT_MILITARY,   0);
        scores.put(CAT_HEALTH,     0);
        scores.put(CAT_TECHNOLOGY, 0);
        scores.put(CAT_ECONOMY,    0);
        scores.put(CAT_POLITICS,   0);

        // 对每个分类计算得分
        for (Map.Entry<Long, Map<String, Integer>> entry : WEIGHTED_KEYWORDS.entrySet()) {
            Long categoryId = entry.getKey();
            Map<String, Integer> keywords = entry.getValue();
            int score = 0;
            for (Map.Entry<String, Integer> kw : keywords.entrySet()) {
                if (text.contains(kw.getKey())) {
                    score += kw.getValue();
                }
            }
            scores.put(categoryId, score);
        }

        // 找最高分
        Long bestCategory = null;
        int bestScore = 0;
        for (Map.Entry<Long, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        if (bestCategory != null && bestScore >= 2) {
            // 至少得 2 分才采用智能分类（防止弱特征词误判）
            log.debug("智能分类: [{}] → 分类[{}] 得分[{}]", title, bestCategory, bestScore);
            return bestCategory;
        }

        // 低于 2 分用来源默认分类
        log.debug("得分不足，使用默认分类[{}]: {}", defaultCategoryId, title);
        return defaultCategoryId;
    }

    private String buildSearchText(String title, String description) {
        StringBuilder sb = new StringBuilder();
        if (title != null) sb.append(title.toLowerCase()).append(" ");
        if (description != null) sb.append(description.toLowerCase());
        return sb.toString();
    }
}