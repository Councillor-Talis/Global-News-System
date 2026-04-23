package com.gns.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gns.common.BusinessException;
import com.gns.dto.ArticleDTO;
import com.gns.entity.Article;
import com.gns.mapper.ArticleMapper;
import com.gns.service.ArticleService;
import com.gns.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final RedisUtil redisUtil;

    private static final String CACHE_PREFIX = "news:list:";
    private static final String HOT_CACHE_KEY = "news:hot";
    private static final long CACHE_SECONDS = 300;

    @Override
    public IPage<Article> listArticles(Integer page, Integer size, Long categoryId, String source) {
        String cacheKey = CACHE_PREFIX + (categoryId == null ? "all" : categoryId) + ":" + size + ":" + page;
        String cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.info("缓存命中: {}", cacheKey);
            return JSON.parseObject(cached, new TypeReference<Page<Article>>() {});
        }
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(categoryId != null && categoryId != 0, Article::getCategoryId, categoryId)
                .eq(source != null && !source.isEmpty(), Article::getSource, source)
                .orderByDesc(Article::getPubTime)
                .orderByDesc(Article::getId);
        IPage<Article> result = articleMapper.selectPage(new Page<>(page, size), wrapper);
        redisUtil.set(cacheKey, JSON.toJSONString(result), CACHE_SECONDS);
        return result;
    }

    @Override
    public Article getDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() == 0) throw new BusinessException("文章不存在");
        articleMapper.incrementViewCount(id);
        return article;
    }

    @Override
    public List<Article> getHotArticles() {
        // 热点也缓存 5 分钟
        String cached = redisUtil.get(HOT_CACHE_KEY);
        if (cached != null) {
            return JSON.parseArray(cached, Article.class);
        }
        List<Article> list = articleMapper.selectTop5ByViewCount();
        redisUtil.set(HOT_CACHE_KEY, JSON.toJSONString(list), CACHE_SECONDS);
        return list;
    }

    @Override
    public IPage<Article> searchArticles(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .and(w -> w
                        .like(Article::getTitle, keyword)
                        .or()
                        .like(Article::getSummary, keyword)
                )
                .orderByDesc(Article::getPubTime);
        return articleMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void createArticle(ArticleDTO dto) {
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        if (article.getPubTime() == null) article.setPubTime(LocalDateTime.now());
        if (article.getStatus() == null) article.setStatus(1);
        articleMapper.insert(article);
        clearCache();
    }

    @Override
    public void updateArticle(Long id, ArticleDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException("文章不存在");
        BeanUtils.copyProperties(dto, article);
        articleMapper.updateById(article);
        clearCache();
    }

    @Override
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
        clearCache();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException("文章不存在");
        article.setStatus(status);
        articleMapper.updateById(article);
        clearCache();
    }

    private void clearCache() {
        redisUtil.deleteByPrefix(CACHE_PREFIX);
        redisUtil.delete(HOT_CACHE_KEY);
    }

    @Override
    public void deleteAllArticles() {
        articleMapper.delete(null);  // null = 不加条件，删除全部
        clearCache();
    }
}