import request from './request'

export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const logout = () => request.post('/user/logout')
export const getProfile = () => request.get('/user/profile')
export const updateUsername = (data) => request.put('/user/username', data)
export const uploadAvatar = (formData) => request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
})