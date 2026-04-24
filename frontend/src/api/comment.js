import request from './request'

export const getCommentList = (articleId) =>
    request.get('/comment/list', { params: { articleId } })

export const getCommentCount = (articleId) =>
    request.get('/comment/count', { params: { articleId } })

export const addComment = (data) => request.post('/comment/add', data)

export const likeComment = (id) => request.post(`/comment/like/${id}`)

export const deleteComment = (id) => request.delete(`/comment/${id}`)