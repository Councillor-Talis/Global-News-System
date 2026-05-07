package com.gns.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gns.common.Result;
import com.gns.dto.ArticleDTO;
import com.gns.entity.Article;
import com.gns.entity.User;
import com.gns.scheduler.NewsCrawlerService;
import com.gns.search.ElasticsearchService;
import com.gns.service.ArticleService;
import com.gns.service.UserService;
import com.gns.vo.StatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ArticleService articleService;
    private final UserService userService;
    private final NewsCrawlerService newsCrawlerService;
    private final ElasticsearchService elasticsearchService;  // ← 加这行

    // ===== 统计数据 =====
    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.success(userService.getStats());
    }

    // ===== 文章管理 =====
    @GetMapping("/article/list")
    public Result<IPage<Article>> articleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String source) {
        return Result.success(articleService.listArticles(page, size, categoryId, source));
    }

    @PostMapping("/article")
    public Result<?> createArticle(@Valid @RequestBody ArticleDTO dto) {
        articleService.createArticle(dto);
        return Result.success("新增成功");
    }

    @PutMapping("/article/{id}")
    public Result<?> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        articleService.updateArticle(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/article/{id}")
    public Result<?> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success("删除成功");
    }

    @PutMapping("/article/{id}/status")
    public Result<?> updateArticleStatus(@PathVariable Long id, @RequestParam Integer status) {
        articleService.updateStatus(id, status);
        return Result.success("操作成功");
    }

    // ===== 用户管理 =====
    @GetMapping("/user/list")
    public Result<IPage<User>> userList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userService.listUsers(page, size));
    }

    @PutMapping("/user/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success("操作成功");
    }

    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    // ===== 采集 =====
    @PostMapping("/crawler/trigger")
    public Result<String> triggerCrawl() {
        String result = newsCrawlerService.manualCrawl();
        return Result.success(result);
    }

    // ===== 文章批量操作 =====
    @DeleteMapping("/article/all")
    public Result<?> deleteAll() {
        articleService.deleteAllArticles();
        return Result.success("已清空所有文章");
    }

    // ===== ES 索引管理 =====
    @PostMapping("/es/reindex")
    public Result<String> reindex() {
        int count = elasticsearchService.indexAllArticles();
        return Result.success("索引重建完成，共索引 " + count + " 篇文章");
    }
}