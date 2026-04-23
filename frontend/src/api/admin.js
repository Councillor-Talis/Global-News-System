import request from './request'

export const getStats = () => request.get('/admin/stats')
export const getAdminArticleList = (params) => request.get('/admin/article/list', { params })
export const createArticle = (data) => request.post('/admin/article', data)
export const updateArticle = (id, data) => request.put(`/admin/article/${id}`, data)
export const deleteArticle = (id) => request.delete(`/admin/article/${id}`)
export const updateArticleStatus = (id, status) =>
    request.put(`/admin/article/${id}/status`, null, { params: { status } })

export const getUserList = (params) => request.get('/admin/user/list', { params })
export const updateUserStatus = (id, status) =>
    request.put(`/admin/user/${id}/status`, null, { params: { status } })

export const triggerCrawl = () => request.post('/admin/crawler/trigger')

export const deleteAllArticles = () => request.delete('/admin/article/all')