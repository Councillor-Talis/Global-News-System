package com.gns.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gns.common.BusinessException;
import com.gns.dto.CommentDTO;
import com.gns.entity.Comment;
import com.gns.entity.CommentLike;
import com.gns.mapper.CommentLikeMapper;
import com.gns.mapper.CommentMapper;
import com.gns.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public List<Comment> listByArticle(Long articleId, Long currentUserId) {
        // 1. 查出该文章所有正常评论
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
        );
        if (all.isEmpty()) return Collections.emptyList();

        // 2. 查询当前用户的点赞记录
        Set<Long> likedIds = new HashSet<>();
        if (currentUserId != null) {
            List<Long> commentIds = all.stream().map(Comment::getId).collect(Collectors.toList());
            commentLikeMapper.selectList(
                    new LambdaQueryWrapper<CommentLike>()
                            .eq(CommentLike::getUserId, currentUserId)
                            .in(CommentLike::getCommentId, commentIds)
            ).forEach(like -> likedIds.add(like.getCommentId()));
        }

        // 3. 标记是否已点赞
        all.forEach(c -> c.setLiked(likedIds.contains(c.getId())));

        // 4. 分离顶级评论和回复
        List<Comment> topComments = all.stream()
                .filter(c -> c.getParentId() == null)
                .sorted((a, b) -> {
                    // 点赞数降序
                    int likeCompare = Integer.compare(
                            b.getLikeCount() == null ? 0 : b.getLikeCount(),
                            a.getLikeCount() == null ? 0 : a.getLikeCount()
                    );
                    if (likeCompare != 0) return likeCompare;
                    // 点赞数相同则时间降序，null 排最后
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .collect(Collectors.toList());

        // 5. 把回复挂到对应的顶级评论下
        Map<Long, List<Comment>> replyMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                })
                .collect(Collectors.groupingBy(Comment::getParentId));

        topComments.forEach(c -> c.setReplies(
                replyMap.getOrDefault(c.getId(), Collections.emptyList())
        ));

        return topComments;
    }

    @Override
    public void addComment(CommentDTO dto, Long userId, String username) {
        // 如果是回复，验证父评论存在
        if (dto.getParentId() != null) {
            Comment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null || parent.getStatus() == 0) {
                throw new BusinessException("被回复的评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(dto.getParentId());
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此评论");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);

        // 同步删除该评论的子回复
        commentMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Comment>()
                        .eq(Comment::getParentId, commentId)
                        .set(Comment::getStatus, 0)
        );
    }

    @Override
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");

        // 查是否已点赞
        Long exists = commentLikeMapper.selectCount(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId)
        );

        if (exists > 0) {
            // 已点赞 → 取消
            commentLikeMapper.delete(
                    new LambdaQueryWrapper<CommentLike>()
                            .eq(CommentLike::getCommentId, commentId)
                            .eq(CommentLike::getUserId, userId)
            );
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(comment);
            return false;  // 返回false = 已取消
        } else {
            // 未点赞 → 点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
            return true;   // 返回true = 已点赞
        }
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