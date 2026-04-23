package com.gns.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gns.common.BusinessException;
import com.gns.common.Result;
import com.gns.dto.CommentDTO;
import com.gns.entity.Comment;
import com.gns.service.CommentService;
import com.gns.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    /** 获取文章评论列表（无需登录） */
    @GetMapping("/list")
    public Result<IPage<Comment>> list(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(commentService.listByArticle(articleId, page, size));
    }

    /** 获取评论数量 */
    @GetMapping("/count")
    public Result<Long> count(@RequestParam Long articleId) {
        return Result.success(commentService.countByArticle(articleId));
    }

    /** 发表评论（需要登录） */
    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody CommentDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(401, "请先登录后再评论");
        }
        Long userId = (Long) auth.getPrincipal();
        // 从 token 里取用户名
        String token = getTokenFromAuth(auth);
        String username = token != null ? jwtUtil.getUsername(token) : "用户" + userId;
        commentService.addComment(dto, userId, username);
        return Result.success("评论成功");
    }

    /** 删除评论（本人或管理员） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(401, "请先登录");
        }
        Long userId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(id, userId, isAdmin);
        return Result.success("删除成功");
    }

    private String getTokenFromAuth(Authentication auth) {
        try {
            Object credentials = auth.getCredentials();
            return credentials != null ? credentials.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}