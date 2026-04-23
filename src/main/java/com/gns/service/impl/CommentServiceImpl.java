package com.gns.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gns.common.BusinessException;
import com.gns.dto.CommentDTO;
import com.gns.entity.Comment;
import com.gns.mapper.CommentMapper;
import com.gns.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    @Override
    public IPage<Comment> listByArticle(Long articleId, Integer page, Integer size) {
        return commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getCreatedAt)
        );
    }

    @Override
    public void addComment(CommentDTO dto, Long userId, String username) {
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(dto.getContent());
        comment.setStatus(1);
        commentMapper.insert(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        // 只有评论作者或管理员可以删除
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此评论");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
    }

    @Override
    public Long countByArticle(Long articleId) {
        return commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
        );
    }
}