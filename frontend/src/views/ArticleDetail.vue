<template>
  <div>
    <NavBar :categories="[]" :showHot="false" />

    <div class="container" v-if="article">
      <div class="back" @click="$router.back()">返回</div>

      <div class="article-wrap">
        <!-- 文章正文 -->
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
          <CommentSection :articleId="article.id" ref="commentRef" />
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="container">
      <el-skeleton :rows="10" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import CommentSection from '../components/CommentSection.vue'
import { getNewsDetail } from '../api/news'
import { getCommentCount } from '../api/comment'

const route = useRoute()
const article = ref(null)
const loading = ref(true)
const commentCount = ref(0)

onMounted(async () => {
  try {
    const [articleRes, countRes] = await Promise.all([
      getNewsDetail(route.params.id),
      getCommentCount(route.params.id)
    ])
    article.value = articleRes.data
    commentCount.value = countRes.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.container { max-width: 800px; margin: 0 auto; padding: 32px 24px; }
.back { 
  display: inline-flex; 
  align-items: center; 
  gap: 6px;
  cursor: pointer; 
  color: #1e293b; 
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 6px 16px;
  margin-bottom: 24px; 
  font-size: 14px; 
  font-weight: 500;
  transition: all 0.2s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.back:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
}
.meta {
  display: flex; gap: 16px; font-size: 13px;
  color: #64748b; margin-bottom: 16px; flex-wrap: wrap;
}
.source { color: #bb1919; font-weight: 600; }
h1 { font-size: 28px; color: #1e293b; line-height: 1.4; margin: 0 0 16px; }
.summary {
  font-size: 16px; color: #475569;
  border-left: 4px solid #3b82f6;
  padding-left: 16px; margin-bottom: 24px;
}
.cover { width: 100%; border-radius: 12px; margin-bottom: 24px; }
.content { font-size: 16px; line-height: 1.8; color: #334155; }
.content :deep(p) { margin-bottom: 16px; }
.content :deep(a) { color: #3b82f6; }
</style>