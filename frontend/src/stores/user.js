import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    // 从 localStorage 恢复登录状态
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => userInfo.value?.role === 1)

    function setUser(data) {
        token.value = data.token
        userInfo.value = data
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(data))
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return { token, userInfo, isLoggedIn, isAdmin, setUser, logout }
})