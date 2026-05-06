package com.gns.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gns.dto.ArticleDTO;
import com.gns.entity.Article;
import java.util.List;
import com.gns.search.SearchResult;

public interface ArticleService {
    IPage<Article> listArticles(Integer page, Integer size, Long categoryId, String source);
    Article getDetail(Long id);
    void createArticle(ArticleDTO dto);
    void updateArticle(Long id, ArticleDTO dto);
    void deleteArticle(Long id);
    void updateStatus(Long id, Integer status);
    void deleteAllArticles();
    List<Article> getHotArticles();                          // 热点文章
    SearchResult searchArticles(String keyword, Integer page, Integer size);
}