package com.gns.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gns.dto.CommentDTO;
import com.gns.entity.Comment;

public interface CommentService {
    IPage<Comment> listByArticle(Long articleId, Integer page, Integer size);
    void addComment(CommentDTO dto, Long userId, String username);
    void deleteComment(Long commentId, Long userId, boolean isAdmin);
    Long countByArticle(Long articleId);
}