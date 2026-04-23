<template>
  <div class="comment-section">

    <!-- 标题 -->
    <div class="section-title">
      <span>评论</span>
      <span class="count">{{ total }}</span>
    </div>

    <!-- 发表评论框 -->
    <div class="comment-input-wrap">
      <template v-if="userStore.isLoggedIn">
        <div class="input-header">
          <el-avatar :size="36" icon="UserFilled" />
          <span class="input-username">{{ userStore.userInfo?.username }}</span>
        </div>
        <el-input
            v-model="content"
            type="textarea"
            :rows="3"
            :maxlength="500"
            show-word-limit
            placeholder="欢迎留言发表你的看法！本系统不会基于政治观点移除任何评论，但请不要发布含有人身攻击或广告的内容。"
            resize="none" />
        <div class="input-footer">
          <span class="community-rules-tip">请注意，发表评论应始终做到尊重他人并遵守<span class="blue-text">社区准则</span>。</span>
          <el-button
              type="primary"
              :loading="submitting"
              :disabled="!content.trim()"
              @click="submitComment">
            发表评论
          </el-button>
        </div>
      </template>

      <!-- 未登录提示 -->
      <template v-else>
        <div class="login-tip">
          <el-icon><ChatDotRound /></el-icon>
          <span>请 <router-link to="/login">登录</router-link> 后发表评论</span>
        </div>
      </template>
    </div>

    <!-- 评论列表 -->
    <div v-if="loading" class="loading">
      <el-skeleton v-for="i in 3" :key="i" :rows="2" animated style="margin-bottom:16px" />
    </div>

    <div v-else-if="comments.length === 0" class="empty">
      暂无评论，来发表第一条吧！
    </div>

    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="36" icon="UserFilled" class="avatar" />
        <div class="comment-body">
          <div class="comment-header">
            <span class="comment-username">{{ comment.username }}</span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            <!-- 本人或管理员可删除 -->
            <span
                v-if="canDelete(comment)"
                class="delete-btn"
                @click="handleDelete(comment.id)">
              删除
            </span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore">
      <el-button text :loading="loadingMore" @click="loadMore">
        加载更多评论
      </el-button>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getCommentList, addComment, deleteComment } from '../api/comment'

const props = defineProps({
  articleId: { type: [Number, String], required: true }
})

const userStore = useUserStore()
const comments = ref([])
const content = ref('')
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = 10

const hasMore = computed(() => comments.value.length < total.value)

onMounted(loadComments)

async function loadComments() {
  loading.value = true
  try {
    const res = await getCommentList({
      articleId: props.articleId,
      page: 1,
      size: pageSize
    })
    comments.value = res.data.records
    total.value = res.data.total
    currentPage.value = 1
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  try {
    currentPage.value++
    const res = await getCommentList({
      articleId: props.articleId,
      page: currentPage.value,
      size: pageSize
    })
    comments.value.push(...res.data.records)
  } finally {
    loadingMore.value = false
  }
}

async function submitComment() {
  if (!content.value.trim()) return
  submitting.value = true
  try {
    await addComment({
      articleId: Number(props.articleId),
      content: content.value.trim()
    })
    ElMessage.success('评论成功')
    content.value = ''
    // 刷新评论列表
    await loadComments()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除这条评论？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteComment(id)
  ElMessage.success('已删除')
  await loadComments()
}

// 判断当前用户是否可以删除该评论（本人或管理员）
function canDelete(comment) {
  if (!userStore.isLoggedIn) return false
  if (userStore.isAdmin) return true
  return userStore.userInfo?.userId === comment.userId
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = (now - d) / 1000 / 60
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${Math.floor(diff)}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  if (diff < 10080) return `${Math.floor(diff / 1440)}天前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.comment-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 2px solid #f1f5f9;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.count {
  background: #f1f5f9;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 20px;
}

/* 输入框 */
.comment-input-wrap {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 32px;
}
.input-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.input-username {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.community-rules-tip {
  font-size: 12px;
  color: #94a3b8;
}
.blue-text {
  color: #3b82f6;
  cursor: pointer;
}
.login-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #64748b;
  font-size: 14px;
}
.login-tip a { color: #3b82f6; text-decoration: none; }

/* 评论列表 */
.comment-list { display: flex; flex-direction: column; gap: 0; }
.comment-item {
  display: flex;
  gap: 12px;
  padding: 20px 0;
  border-bottom: 1px solid #f1f5f9;
}
.comment-item:last-child { border-bottom: none; }
.avatar { flex-shrink: 0; margin-top: 2px; }
.comment-body { flex: 1; min-width: 0; }
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.comment-username {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
.comment-time {
  font-size: 12px;
  color: #94a3b8;
}
.delete-btn {
  font-size: 12px;
  color: #ef4444;
  cursor: pointer;
  margin-left: auto;
}
.delete-btn:hover { text-decoration: underline; }
.comment-content {
  font-size: 15px;
  color: #334155;
  line-height: 1.7;
  word-break: break-word;
}

/* 其他 */
.empty {
  text-align: center;
  color: #94a3b8;
  padding: 40px 0;
  font-size: 14px;
}
.load-more {
  text-align: center;
  padding: 16px 0;
}
.loading { padding: 16px 0; }
</style>