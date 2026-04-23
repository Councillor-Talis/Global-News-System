package com.gns.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gns.common.Result;
import com.gns.entity.Article;
import com.gns.entity.Category;
import com.gns.service.ArticleService;
import com.gns.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NewsController {

    private final ArticleService articleService;
    private final CategoryService categoryService;

    @GetMapping("/news/list")
    public Result<IPage<Article>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "9") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String source) {
        return Result.success(articleService.listArticles(page, size, categoryId, source));
    }

    @GetMapping("/news/detail/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    // 热点文章（浏览量前5）
    @GetMapping("/news/hot")
    public Result<List<Article>> hot() {
        return Result.success(articleService.getHotArticles());
    }

    // 搜索
    @GetMapping("/news/search")
    public Result<IPage<Article>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "9") Integer size) {
        return Result.success(articleService.searchArticles(keyword, page, size));
    }

    @GetMapping("/category/list")
    public Result<List<Category>> categories() {
        return Result.success(categoryService.listAll());
    }
}