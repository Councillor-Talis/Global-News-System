package com.gns.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;

@Slf4j
@Component
public class ArticleCategorizer {

    // 分类 ID
    private static final long CAT_POLITICS     = 2L;
    private static final long CAT_ECONOMY      = 3L;
    private static final long CAT_BUSINESS     = 4L;
    private static final long CAT_TECHNOLOGY   = 5L;
    private static final long CAT_MILITARY     = 6L;
    private static final long CAT_SOCIETY      = 7L;
    private static final long CAT_CULTURE      = 8L;
    private static final long CAT_ENTERTAINMENT= 9L;
    private static final long CAT_SPORTS       = 10L;
    private static final long CAT_HEALTH       = 11L;
    private static final long CAT_ENVIRONMENT  = 12L;

    private static final Map<Long, Map<String, Integer>> KEYWORDS = new LinkedHashMap<>();

    static {

        // ===== 体育（强特征极明显，优先匹配）=====
        Map<String, Integer> sports = new LinkedHashMap<>();
        sports.put("world cup", 3); sports.put("nba", 3); sports.put("nfl", 3);
        sports.put("premier league", 3); sports.put("champions league", 3);
        sports.put("olympic", 3); sports.put("formula 1", 3); sports.put("grand prix", 3);
        sports.put("wimbledon", 3); sports.put("us open", 3); sports.put("french open", 3);
        sports.put("australian open", 3); sports.put("super bowl", 3);
        sports.put("fifa", 3); sports.put("uefa", 3); sports.put("nhl", 3);
        sports.put("mlb", 3); sports.put("tour de france", 3);
        sports.put("championship", 2); sports.put("tournament", 2);
        sports.put("match", 2); sports.put("athlete", 2); sports.put("stadium", 2);
        sports.put("coach", 2); sports.put("transfer", 2); sports.put("player", 2);
        sports.put("league", 2); sports.put("season", 2); sports.put("score", 2);
        sports.put("goal", 2); sports.put("medal", 2); sports.put("race", 2);
        sports.put("sport", 1); sports.put("football", 1); sports.put("basketball", 1);
        sports.put("tennis", 1); sports.put("golf", 1); sports.put("baseball", 1);
        sports.put("cricket", 1); sports.put("rugby", 1); sports.put("swimming", 1);
        sports.put("athletics", 1);
        KEYWORDS.put(CAT_SPORTS, sports);

        // ===== 娱乐 =====
        Map<String, Integer> entertainment = new LinkedHashMap<>();
        entertainment.put("box office", 3); entertainment.put("oscar", 3);
        entertainment.put("grammy", 3); entertainment.put("bafta", 3);
        entertainment.put("golden globe", 3); entertainment.put("emmy", 3);
        entertainment.put("box office", 3); entertainment.put("blockbuster", 3);
        entertainment.put("netflix", 3); entertainment.put("disney+", 3);
        entertainment.put("hollywood", 3); entertainment.put("taylor swift", 3);
        entertainment.put("beyonce", 3); entertainment.put("celebrity", 2);
        entertainment.put("movie", 2); entertainment.put("film", 2);
        entertainment.put("album", 2); entertainment.put("concert", 2);
        entertainment.put("singer", 2); entertainment.put("actor", 2);
        entertainment.put("actress", 2); entertainment.put("director", 2);
        entertainment.put("streaming", 2); entertainment.put("tv show", 2);
        entertainment.put("series", 2); entertainment.put("premiere", 2);
        entertainment.put("music", 1); entertainment.put("entertainment", 1);
        entertainment.put("award", 1); entertainment.put("star", 1);
        KEYWORDS.put(CAT_ENTERTAINMENT, entertainment);

        // ===== 环境 =====
        Map<String, Integer> environment = new LinkedHashMap<>();
        environment.put("climate change", 3); environment.put("global warming", 3);
        environment.put("carbon emission", 3); environment.put("greenhouse gas", 3);
        environment.put("renewable energy", 3); environment.put("solar panel", 3);
        environment.put("wind farm", 3); environment.put("deforestation", 3);
        environment.put("biodiversity", 3); environment.put("endangered species", 3);
        environment.put("paris agreement", 3); environment.put("cop28", 3);
        environment.put("cop29", 3); environment.put("ipcc", 3);
        environment.put("wildfire", 3); environment.put("drought", 3);
        environment.put("flood", 2); environment.put("pollution", 2);
        environment.put("plastic waste", 2); environment.put("ocean", 2);
        environment.put("glacier", 2); environment.put("arctic", 2);
        environment.put("ecosystem", 2); environment.put("sustainability", 2);
        environment.put("fossil fuel", 2); environment.put("net zero", 2);
        environment.put("carbon", 1); environment.put("emission", 1);
        environment.put("environment", 1); environment.put("green", 1);
        environment.put("climate", 1);
        KEYWORDS.put(CAT_ENVIRONMENT, environment);

        // ===== 健康 =====
        Map<String, Integer> health = new LinkedHashMap<>();
        health.put("clinical trial", 3); health.put("fda approved", 3);
        health.put("world health organization", 3); health.put("pandemic", 3);
        health.put("outbreak", 3); health.put("cancer treatment", 3);
        health.put("mental health", 3); health.put("public health", 3);
        health.put("drug overdose", 3); health.put("alzheimer", 3);
        health.put("dementia", 3); health.put("opioid", 3);
        health.put("vaccine efficacy", 3); health.put("virus", 2);
        health.put("disease", 2); health.put("cancer", 2);
        health.put("vaccine", 2); health.put("hospital", 2);
        health.put("medicine", 2); health.put("treatment", 2);
        health.put("patient", 2); health.put("surgery", 2);
        health.put("diabetes", 2); health.put("obesity", 2);
        health.put("epidemic", 2); health.put("infection", 2);
        health.put("symptom", 2); health.put("pharmaceutical", 2);
        health.put("biotech", 2); health.put("cdc", 2); health.put("nih", 2);
        health.put("health", 1); health.put("doctor", 1);
        health.put("drug", 1); health.put("medical", 1); health.put("therapy", 1);
        KEYWORDS.put(CAT_HEALTH, health);

        // ===== 军事 / 战争 / 灾害 =====
        Map<String, Integer> military = new LinkedHashMap<>();
        military.put("missile strike", 3); military.put("air strike", 3);
        military.put("military operation", 3); military.put("nuclear weapon", 3);
        military.put("ceasefire", 3); military.put("warship", 3);
        military.put("fighter jet", 3); military.put("drone attack", 3);
        military.put("suicide bomber", 3); military.put("war crime", 3);
        military.put("earthquake", 3); military.put("tsunami", 3);
        military.put("hurricane", 3); military.put("typhoon", 3);
        military.put("magnitude", 3); military.put("richter", 3);
        military.put("evacuation order", 3); military.put("artillery", 3);
        military.put("airstrike", 2); military.put("military", 2);
        military.put("combat", 2); military.put("invasion", 2);
        military.put("nato", 2); military.put("pentagon", 2);
        military.put("troops", 2); military.put("casualties", 2);
        military.put("hostage", 2); military.put("terror", 2);
        military.put("explosion", 2); military.put("disaster", 2);
        military.put("cyclone", 2); military.put("landslide", 2);
        military.put("war", 1); military.put("army", 1);
        military.put("soldier", 1); military.put("bomb", 1);
        military.put("attack", 1); military.put("conflict", 1);
        military.put("defense", 1); military.put("weapon", 1);
        KEYWORDS.put(CAT_MILITARY, military);

        // ===== 科技 =====
        Map<String, Integer> tech = new LinkedHashMap<>();
        tech.put("artificial intelligence", 3); tech.put("machine learning", 3);
        tech.put("large language model", 3); tech.put("chatgpt", 3);
        tech.put("openai", 3); tech.put("generative ai", 3);
        tech.put("semiconductor", 3); tech.put("silicon valley", 3);
        tech.put("quantum computing", 3); tech.put("self-driving", 3);
        tech.put("cybersecurity breach", 3); tech.put("data breach", 3);
        tech.put("spacex launch", 3); tech.put("autonomous vehicle", 3);
        tech.put("google", 2); tech.put("apple", 2); tech.put("microsoft", 2);
        tech.put("meta", 2); tech.put("nvidia", 2); tech.put("software", 2);
        tech.put("startup", 2); tech.put("algorithm", 2);
        tech.put("cybersecurity", 2); tech.put("smartphone", 2);
        tech.put("5g", 2); tech.put("chip", 2); tech.put("robot", 2);
        tech.put("iphone", 2); tech.put("android", 2); tech.put("hack", 2);
        tech.put("technology", 1); tech.put("tech", 1);
        tech.put("digital", 1); tech.put("internet", 1);
        tech.put("cloud", 1); tech.put("ai ", 1);
        KEYWORDS.put(CAT_TECHNOLOGY, tech);

        // ===== 商业 =====
        Map<String, Integer> business = new LinkedHashMap<>();
        business.put("ipo", 3); business.put("merger", 3);
        business.put("acquisition", 3); business.put("layoffs", 3);
        business.put("quarterly earnings", 3); business.put("revenue growth", 3);
        business.put("stock price", 3); business.put("market cap", 3);
        business.put("ceo", 3); business.put("startup funding", 3);
        business.put("venture capital", 3); business.put("hedge fund", 3);
        business.put("wall street", 3); business.put("nasdaq", 3);
        business.put("s&p 500", 3); business.put("dow jones", 3);
        business.put("amazon", 2); business.put("tesla", 2);
        business.put("company", 2); business.put("corporation", 2);
        business.put("profit", 2); business.put("loss", 2);
        business.put("revenue", 2); business.put("earnings", 2);
        business.put("investment", 2); business.put("investor", 2);
        business.put("stock", 2); business.put("shares", 2);
        business.put("market", 1); business.put("business", 1);
        business.put("industry", 1); business.put("firm", 1);
        KEYWORDS.put(CAT_BUSINESS, business);

        // ===== 经济 =====
        Map<String, Integer> economy = new LinkedHashMap<>();
        economy.put("federal reserve", 3); economy.put("interest rate", 3);
        economy.put("gdp growth", 3); economy.put("inflation rate", 3);
        economy.put("central bank", 3); economy.put("trade deficit", 3);
        economy.put("trade surplus", 3); economy.put("supply chain", 3);
        economy.put("oil price", 3); economy.put("opec", 3);
        economy.put("imf", 3); economy.put("world bank", 3);
        economy.put("recession", 3); economy.put("monetary policy", 3);
        economy.put("fiscal policy", 3); economy.put("unemployment rate", 3);
        economy.put("economy", 2); economy.put("economic", 2);
        economy.put("trade", 2); economy.put("inflation", 2);
        economy.put("tariff", 2); economy.put("employment", 2);
        economy.put("tax", 2); economy.put("budget", 2);
        economy.put("debt", 2); economy.put("currency", 2);
        economy.put("dollar", 2); economy.put("euro", 2);
        economy.put("bond", 2); economy.put("export", 2); economy.put("import", 2);
        economy.put("growth", 1); economy.put("billion", 1); economy.put("trillion", 1);
        KEYWORDS.put(CAT_ECONOMY, economy);

        // ===== 文化 =====
        Map<String, Integer> culture = new LinkedHashMap<>();
        culture.put("museum", 3); culture.put("art exhibition", 3);
        culture.put("heritage site", 3); culture.put("world heritage", 3);
        culture.put("literature", 3); culture.put("nobel prize literature", 3);
        culture.put("fashion week", 3); culture.put("architecture", 3);
        culture.put("cultural festival", 3); culture.put("religion", 2);
        culture.put("tradition", 2); culture.put("language", 2);
        culture.put("history", 2); culture.put("archaeology", 2);
        culture.put("art", 2); culture.put("painting", 2);
        culture.put("sculpture", 2); culture.put("exhibition", 2);
        culture.put("culture", 1); culture.put("book", 1);
        culture.put("author", 1); culture.put("poetry", 1);
        KEYWORDS.put(CAT_CULTURE, culture);

        // ===== 社会 =====
        Map<String, Integer> society = new LinkedHashMap<>();
        society.put("crime rate", 3); society.put("police shooting", 3);
        society.put("human rights", 3); society.put("immigration", 3);
        society.put("refugee crisis", 3); society.put("protest march", 3);
        society.put("racial inequality", 3); society.put("gender equality", 3);
        society.put("poverty", 3); society.put("homelessness", 3);
        society.put("gun violence", 3); society.put("mass shooting", 3);
        society.put("court ruling", 2); society.put("prison", 2);
        society.put("arrested", 2); society.put("murdered", 2);
        society.put("community", 2); society.put("education", 2);
        society.put("school", 2); society.put("university", 2);
        society.put("discrimination", 2); society.put("protest", 2);
        society.put("demonstration", 2); society.put("charity", 2);
        society.put("social", 1); society.put("people", 1);
        society.put("family", 1); society.put("children", 1);
        KEYWORDS.put(CAT_SOCIETY, society);

        // ===== 政治（最后兜底）=====
        Map<String, Integer> politics = new LinkedHashMap<>();
        politics.put("presidential election", 3); politics.put("general election", 3);
        politics.put("prime minister", 3); politics.put("secretary of state", 3);
        politics.put("united nations", 3); politics.put("security council", 3);
        politics.put("white house", 3); politics.put("congress", 3);
        politics.put("senate", 3); politics.put("supreme court", 3);
        politics.put("diplomatic", 3); politics.put("sanction", 3);
        politics.put("geopolitical", 3); politics.put("bilateral", 3);
        politics.put("president", 2); politics.put("government", 2);
        politics.put("election", 2); politics.put("vote", 2);
        politics.put("policy", 2); politics.put("legislation", 2);
        politics.put("parliament", 2); politics.put("democrat", 2);
        politics.put("republican", 2); politics.put("summit", 2);
        politics.put("treaty", 2); politics.put("minister", 2);
        politics.put("ambassador", 2); politics.put("biden", 2);
        politics.put("trump", 2); politics.put("xi jinping", 2);
        politics.put("putin", 2); politics.put("zelensky", 2);
        politics.put("political", 1); politics.put("law", 1);
        politics.put("opposition", 1);
        KEYWORDS.put(CAT_POLITICS, politics);
    }

    /**
     * 加权评分，最高分胜出，最低 3 分才采用智能分类
     */
    public Long categorize(String title, String description, Long defaultCategoryId) {
        String text = buildText(title, description);
        if (text.isEmpty()) return defaultCategoryId;

        Map<Long, Integer> scores = new LinkedHashMap<>();
        for (Long catId : KEYWORDS.keySet()) scores.put(catId, 0);

        for (Map.Entry<Long, Map<String, Integer>> entry : KEYWORDS.entrySet()) {
            int score = 0;
            for (Map.Entry<String, Integer> kw : entry.getValue().entrySet()) {
                if (text.contains(kw.getKey())) score += kw.getValue();
            }
            scores.put(entry.getKey(), score);
        }

        Long best = null;
        int bestScore = 0;
        for (Map.Entry<Long, Integer> e : scores.entrySet()) {
            if (e.getValue() > bestScore) {
                bestScore = e.getValue();
                best = e.getKey();
            }
        }

        // 至少 3 分才信任智能分类
        if (best != null && bestScore >= 3) {
            log.debug("智能分类: [{}] → 分类[{}] 得分[{}]", title, best, bestScore);
            return best;
        }

        log.debug("得分不足({})，使用默认分类[{}]: {}", bestScore, defaultCategoryId, title);
        return defaultCategoryId;
    }

    private String buildText(String title, String description) {
        StringBuilder sb = new StringBuilder();
        if (title != null) sb.append(title.toLowerCase()).append(" ");
        if (description != null) sb.append(description.toLowerCase());
        return sb.toString();
    }
}