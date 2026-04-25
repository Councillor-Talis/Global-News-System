package com.gns.service;

import com.gns.dto.CommentDTO;
import com.gns.entity.Comment;
import java.util.List;

public interface CommentService {
    // 获取文章评论（顶级评论按点赞排序，每条带子回复）
    List<Comment> listByArticle(Long articleId, Long currentUserId);

    // 发表评论或回复
    void addComment(CommentDTO dto, Long userId, String username);

    // 删除评论
    void deleteComment(Long commentId, Long userId, boolean isAdmin);

    // 点赞 / 取消点赞
    boolean toggleLike(Long commentId, Long userId);

    // 评论总数
    Long countByArticle(Long articleId);
}