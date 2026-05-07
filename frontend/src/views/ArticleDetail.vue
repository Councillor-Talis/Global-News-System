<template>
  <div>
    <NavBar :categories="[]" />

    <div class="page-container" v-if="article">
      <div class="back" @click="$router.back()">← 返回</div>

      <div class="layout">

        <!-- 左侧：文章正文 -->
        <div class="main">
          <div class="meta">
            <span class="source">{{ article.source }}</span>
            <span class="time">{{ article.pubTime }}</span>
            <span class="views">👁 {{ article.viewCount }} 次阅读</span>
            <span class="comment-count">💬 {{ commentCount }} 条评论</span>
          </div>

          <h1>{{ article.title }}</h1>

          <p class="summary">{{ article.summary }}</p>

          <img v-if="article.coverImg" :src="article.coverImg" class="cover" />

          <div class="content" v-html="article.content"></div>

          <!-- 评论区 -->
          <CommentSection :articleId="article.id" />
        </div>

        <!-- 右侧：相关推荐 -->
        <div class="sidebar">
          <div class="sidebar-title">相关推荐</div>

          <div v-if="relatedLoading" class="related-loading">
            <el-skeleton v-for="i in 4" :key="i" :rows="2" animated
                         style="margin-bottom:16px" />
          </div>

          <div v-else-if="relatedArticles.length === 0" class="related-empty">
            暂无相关文章
          </div>

          <div v-else class="related-list">
            <div
                v-for="item in relatedArticles"
                :key="item.id"
                class="related-item"
                @click="goArticle(item.id)">

              <!-- 封面图 -->
              <div class="related-cover">
                <img
                    :src="item.coverImg || defaultImg"
                    :alt="item.title"
                    @error="onImgError" />
              </div>

              <!-- 文字信息 -->
              <div class="related-info">
                <div class="related-source">{{ item.source }}</div>
                <div class="related-title">{{ item.title }}</div>
                <div class="related-time">{{ formatTime(item.pubTime) }}</div>
              </div>

            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 加载骨架屏 -->
    <div v-else-if="loading" class="page-container">
      <el-skeleton :rows="10" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import CommentSection from '../components/CommentSection.vue'
import { getNewsDetail, getRelatedNews } from '../api/news'
import { getCommentCount } from '../api/comment'

const route = useRoute()
const router = useRouter()

const article = ref(null)
const loading = ref(true)
const commentCount = ref(0)
const relatedArticles = ref([])
const relatedLoading = ref(false)
const defaultImg = 'https://picsum.photos/seed/default/400/240'

onMounted(() => loadArticle())

// 路由变化时重新加载（从相关推荐跳转时触发）
watch(() => route.params.id, () => {
  article.value = null
  loading.value = true
  loadArticle()
})

async function loadArticle() {
  try {
    const [articleRes, countRes] = await Promise.all([
      getNewsDetail(route.params.id),
      getCommentCount(route.params.id)
    ])
    article.value = articleRes.data
    commentCount.value = countRes.data
    // 加载完文章后再加载相关推荐
    loadRelated()
  } finally {
    loading.value = false
  }
}

async function loadRelated() {
  if (!article.value?.categoryId) return
  relatedLoading.value = true
  try {
    const res = await getRelatedNews(article.value.categoryId, article.value.id)
    relatedArticles.value = res.data
  } finally {
    relatedLoading.value = false
  }
}

function goArticle(id) {
  router.push(`/article/${id}`)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onImgError(e) {
  e.target.src = defaultImg
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = (now - d) / 1000 / 60
  if (diff < 60) return `${Math.floor(diff)}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

.back {
  cursor: pointer;
  color: #3b82f6;
  margin-bottom: 24px;
  font-size: 14px;
  display: inline-block;
}

/* 左右布局 */
.layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

/* 左侧正文 */
.main {
  flex: 1;
  min-width: 0;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.source { color: #bb1919; font-weight: 600; }

h1 {
  font-size: 26px;
  color: #1e293b;
  line-height: 1.4;
  margin: 0 0 16px;
}

.summary {
  font-size: 16px;
  color: #475569;
  border-left: 4px solid #3b82f6;
  padding-left: 16px;
  margin-bottom: 24px;
  line-height: 1.7;
}

.cover {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 24px;
  object-fit: cover;
  max-height: 460px;
}

.content {
  font-size: 16px;
  line-height: 1.8;
  color: #334155;
}
.content :deep(p) { margin-bottom: 16px; }
.content :deep(a) { color: #3b82f6; }

/* 右侧边栏 */
.sidebar {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;   /* 吸附在顶部导航下方 */
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
}

/* 相关文章列表 */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.related-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 10px;
  border-radius: 10px;
  transition: background .15s;
  border: 1px solid #f1f5f9;
}
.related-item:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
}

/* 封面图 */
.related-cover {
  width: 90px;
  height: 60px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}
.related-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}
.related-item:hover .related-cover img {
  transform: scale(1.05);
}

/* 文字 */
.related-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.related-source {
  font-size: 11px;
  color: #bb1919;
  font-weight: 600;
}
.related-title {
  font-size: 13px;
  color: #1e293b;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-weight: 500;
}
.related-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: auto;
}

.related-empty {
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  padding: 24px 0;
}

.related-loading { padding: 8px 0; }

/* 响应式：小屏隐藏侧边栏 */
@media (max-width: 900px) {
  .sidebar { display: none; }
}
</style>