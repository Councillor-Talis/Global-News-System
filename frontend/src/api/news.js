import request from './request'

export const getNewsList = (params) => request.get('/news/list', { params })
export const getNewsDetail = (id) => request.get(`/news/detail/${id}`)
export const getCategoryList = () => request.get('/category/list')
export const getHotNews = () => request.get('/news/hot')
export const searchNews = (params) => request.get('/news/search', { params })