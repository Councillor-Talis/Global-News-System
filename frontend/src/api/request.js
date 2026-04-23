import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import router from '../router'

const request = axios.create({
    baseURL: '/api',        // 通过 Vite proxy 转发到后端 8080
    timeout: 10000,
})

// 请求拦截器：自动带上 token
request.interceptors.request.use(config => {
    const userStore = useUserStore()
    if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(res)
        }
        return res
    },
    error => {
        if (error.response?.status === 401) {
            const userStore = useUserStore()
            userStore.logout()
            router.push('/login')
            ElMessage.error('登录已过期，请重新登录')
        } else {
            ElMessage.error(error.response?.data?.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request