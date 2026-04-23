import request from './request'

// 获取评论列表
export const getCommentList = (params) => request.get('/comment/list', { params })

// 获取评论数量
export const getCommentCount = (articleId) => request.get('/comment/count', { params: { articleId } })

// 发表评论
export const addComment = (data) => request.post('/comment/add', data)

// 删除评论
export const deleteComment = (id) => request.delete(`/comment/${id}`)