import request from './request'

// 登录
export const login = (data) => request.post('/user/login', data)

// 注册
export const register = (data) => request.post('/user/register', data)

// 退出
export const logout = () => request.post('/user/logout')