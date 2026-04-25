package com.gns.controller;

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

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    /** 获取文章评论列表（无需登录，已登录时返回点赞状态） */
    @GetMapping("/list")
    public Result<List<Comment>> list(@RequestParam Long articleId) {
        Long currentUserId = getCurrentUserId();
        return Result.success(commentService.listByArticle(articleId, currentUserId));
    }

    /** 获取评论数量 */
    @GetMapping("/count")
    public Result<Long> count(@RequestParam Long articleId) {
        return Result.success(commentService.countByArticle(articleId));
    }

    /** 发表评论或回复 */
    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody CommentDTO dto) {
        Long userId = requireLogin();
        String username = getUsername();
        commentService.addComment(dto, userId, username);
        return Result.success("评论成功");
    }

    /** 点赞 / 取消点赞 */
    @PostMapping("/like/{id}")
    public Result<Boolean> like(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean liked = commentService.toggleLike(id, userId);
        return Result.success(liked ? "点赞成功" : "已取消点赞", liked);
    }

    /** 删除评论 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = requireLogin();
        boolean isAdmin = getAuth().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(id, userId, isAdmin);
        return Result.success("删除成功");
    }

    // ===== 私有工具方法 =====

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Long getCurrentUserId() {
        Authentication auth = getAuth();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) return null;
        try { return (Long) auth.getPrincipal(); } catch (Exception e) { return null; }
    }

    private Long requireLogin() {
        Long userId = getCurrentUserId();
        if (userId == null) throw new BusinessException(401, "请先登录");
        return userId;
    }

    private String getUsername() {
        Authentication auth = getAuth();
        try {
            String token = auth.getCredentials().toString();
            return jwtUtil.getUsername(token);
        } catch (Exception e) {
            return "用户" + getCurrentUserId();
        }
    }
}