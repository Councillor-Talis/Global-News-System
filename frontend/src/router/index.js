import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
    { path: '/', component: () => import('../views/Home.vue') },
    { path: '/article/:id', component: () => import('../views/ArticleDetail.vue') },
    { path: '/search', component: () => import('../views/Search.vue') },
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/register', component: () => import('../views/Register.vue') },
    { path: '/profile', component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
    {
        path: '/admin',
        component: () => import('../views/admin/AdminLayout.vue'),
        meta: { requiresAuth: true, requiresAdmin: true },
        children: [
            { path: '', redirect: '/admin/dashboard' },
            { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
            { path: 'articles', component: () => import('../views/admin/ArticleManage.vue') },
            { path: 'users', component: () => import('../views/admin/UserManage.vue') },
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to) => {
    const userStore = useUserStore()
    if (to.meta.requiresAuth && !userStore.isLoggedIn) return '/login'
    if (to.meta.requiresAdmin && !userStore.isAdmin) return '/'
})

export default router