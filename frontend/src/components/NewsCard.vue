<template>
  <div class="card" @click="$router.push(`/article/${article.id}`)">
    <div class="cover">
      <img :src="article.coverImg || defaultImg" :alt="article.title"/>
      <span class="source-badge" :style="{ background: sourceColor }">
        {{ article.source }}
      </span>
    </div>
    <div class="body">
      <div class="meta">
        <span class="category">{{ categoryName }}</span>
        <span class="time">{{ formatTime(article.pubTime) }}</span>
      </div>
      <h3 class="title" v-html="highlight(article.title)"></h3>
      <p class="summary" v-html="highlight(article.summary)"></p>
    </div>
    <div class="footer">
      <div class="footer-item">
        <el-icon><View/></el-icon>
        <span>{{ article.viewCount || 0 }}</span>
      </div>
      <div class="footer-item">
        <el-icon><ChatDotRound/></el-icon>
        <span>{{ commentCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCommentCount } from '../api/comment'

const props = defineProps({
  article: Object,
  categoryName: String,
  keyword: {type: String, default: ''},  // 搜索关键词，用于高亮
})

const commentCount = ref(0)

onMounted(async () => {
  if (props.article?.id) {
    try {
      const res = await getCommentCount(props.article.id)
      commentCount.value = res.data
    } catch (e) {
      // ignore
    }
  }
})

const defaultImg = 'https://picsum.photos/seed/default/800/450'

const sourceColors = {
  'BBC News': '#bb1919',
  'Reuters': '#ff8000',
  'AP News': '#005eb8',
}
const sourceColor = sourceColors[props.article?.source] || '#475569'

// 关键词高亮（仅在搜索结果页生效）
function highlight(text) {
  if (!text || !props.keyword) return text
  const reg = new RegExp(`(${props.keyword})`, 'gi')
  return text.replace(reg, '<mark>$1</mark>')
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
.card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  cursor: pointer;
  transition: all .25s;
  display: flex;
  flex-direction: column;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.cover {
  position: relative;
  aspect-ratio: 16/9;
  overflow: hidden;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s;
}

.card:hover .cover img {
  transform: scale(1.05);
}

.source-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 4px;
}

.body {
  padding: 14px 16px;
  flex: 1;
}

.meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.category {
  font-size: 12px;
  color: #3b82f6;
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: #94a3b8;
}

.title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.summary {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.footer {
  padding: 10px 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #94a3b8;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 搜索高亮样式 */
:deep(mark) {
  background: #fef08a;
  color: #1e293b;
  border-radius: 2px;
  padding: 0 1px;
}
</style>