<template>
  <div class="navbar">
    <div class="nav-inner">

      <!-- Logo -->
      <router-link to="/" class="logo">GNS</router-link>

      <!-- 分类 Tab + 热点 -->
      <div class="tabs">
        <!-- 普通分类 -->
        <span
            v-for="cat in categories" :key="cat.id"
            class="tab"
            :class="{ active: activeCategoryId === cat.id && !hotMode }"
            @click="onCategoryClick(cat.id)">
          {{ cat.name }}
        </span>

        <!-- 热点分栏 -->
        <div class="hot-wrap" ref="hotRef" v-if="showHot">
          <span
              class="tab"
              :class="{ active: hotMode }"
              @click="toggleHot">
            热点
          </span>

          <!-- 热点下拉面板 -->
          <transition name="fade">
            <div class="hot-panel" v-if="hotOpen">
              <div class="hot-title">热点新闻 TOP 5</div>
              <div v-if="hotLoading" class="hot-loading">
                <el-skeleton :rows="3" animated />
              </div>
              <div
                  v-for="(item, index) in hotList" :key="item.id"
                  class="hot-item"
                  @click="goArticle(item.id)">
                <span class="hot-rank" :class="'rank-' + (index + 1)">
                  {{ index + 1 }}
                </span>
                <div class="hot-info">
                  <div class="hot-text">{{ item.title }}</div>
                  <div class="hot-meta">
                    <span>{{ item.source }}</span>
                    <span>👁 {{ item.viewCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 搜索框 + 右侧按钮 -->
      <div class="nav-right">
        <!-- 搜索 -->
        <div class="search-wrap">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索新闻..."
              size="small"
              style="width: 180px"
              clearable
              @keyup.enter="doSearch"
              @clear="doSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 用户信息/登录按钮 -->
        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="28" icon="UserFilled" />
              <span>{{ userStore.userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login">
            <el-button type="primary" size="small">登录</el-button>
          </router-link>
        </template>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { logout } from '../api/user'
import { getHotNews } from '../api/news'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  activeCategoryId: [Number, null],
  showHot: { type: Boolean, default: true }
})
const emit = defineEmits(['categoryChange', 'hotModeChange'])

const router = useRouter()
const userStore = useUserStore()

// 搜索
const searchKeyword = ref('')
function doSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  router.push({ path: '/search', query: { keyword: kw } })
}

// 分类切换
const hotMode = ref(false)
function onCategoryClick(id) {
  hotMode.value = false
  hotOpen.value = false
  emit('categoryChange', id)
}

// 热点面板
const hotOpen = ref(false)
const hotLoading = ref(false)
const hotList = ref([])
const hotRef = ref(null)

async function toggleHot() {
  hotMode.value = true
  hotOpen.value = !hotOpen.value
  emit('hotModeChange', hotOpen.value)
  if (hotOpen.value && hotList.value.length === 0) {
    hotLoading.value = true
    try {
      const res = await getHotNews()
      hotList.value = res.data
    } finally {
      hotLoading.value = false
    }
  }
}

function goArticle(id) {
  hotOpen.value = false
  router.push(`/article/${id}`)
}

// 点击面板外关闭
function handleClickOutside(e) {
  if (hotRef.value && !hotRef.value.contains(e.target)) {
    hotOpen.value = false
  }
}
onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))

// 退出
async function handleLogout() {
  try { await logout() } catch {}
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.navbar {
  position: sticky; top: 0; z-index: 100;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.nav-inner {
  max-width: 1280px; margin: 0 auto;
  padding: 0 24px; height: 60px;
  display: flex; align-items: center; gap: 24px;
}
.logo {
  font-size: 22px; font-weight: 900;
  color: #1e293b; text-decoration: none;
  letter-spacing: -1px; flex-shrink: 0;
}

/* 分类 Tab */
.tabs { display: flex; align-items: center; gap: 2px; flex: 1; overflow: hidden; }
.tab {
  padding: 5px 12px; border-radius: 20px; white-space: nowrap;
  cursor: pointer; font-size: 14px; color: #64748b;
  transition: all .2s;
}
.tab:hover { background: #f1f5f9; color: #1e293b; }
.tab.active { background: #1e293b; color: #fff; }

/* 热点 tab */
.hot-wrap { position: relative; }

/* 热点下拉面板 */
.hot-panel {
  position: absolute; top: calc(100% + 8px); left: 0;
  width: 340px; background: #fff;
  border: 1px solid #e2e8f0; border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
  padding: 12px; z-index: 200;
}
.hot-title {
  font-size: 13px; font-weight: 600; color: #64748b;
  padding: 4px 8px 10px; border-bottom: 1px solid #f1f5f9;
  margin-bottom: 8px;
}
.hot-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 8px; border-radius: 8px; cursor: pointer;
  transition: background .15s;
}
.hot-item:hover { background: #f8fafc; }
.hot-rank {
  flex-shrink: 0; width: 22px; height: 22px;
  border-radius: 6px; display: flex; align-items: center;
  justify-content: center; font-size: 12px; font-weight: 700;
  background: #f1f5f9; color: #94a3b8;
}
.rank-1 { background: #ef4444; color: #fff; }
.rank-2 { background: #f97316; color: #fff; }
.rank-3 { background: #eab308; color: #fff; }
.hot-info { flex: 1; min-width: 0; }
.hot-text {
  font-size: 13px; color: #1e293b; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}
.hot-meta {
  display: flex; gap: 8px; margin-top: 4px;
  font-size: 11px; color: #94a3b8;
}
.hot-loading { padding: 8px; }

/* 搜索 */
.nav-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.search-wrap { display: flex; align-items: center; }
.user-info {
  display: flex; align-items: center; gap: 6px;
  cursor: pointer; font-size: 14px; color: #1e293b;
}

/* 动画 */
.fade-enter-active, .fade-leave-active { transition: all .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-6px); }
</style>