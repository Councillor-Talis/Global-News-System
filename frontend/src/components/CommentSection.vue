<template>
  <div class="comment-section">

    <!-- 标题栏 -->
    <div class="section-title">
      <span>评论</span>
      <span class="count">{{ total }}</span>
    </div>

    <!-- 发表评论框 -->
    <div class="comment-input-wrap">
      <template v-if="userStore.isLoggedIn">
        <div class="input-row">
          <el-avatar
              :size="36"
              :src="userStore.userInfo?.avatar || ''"
              :icon="userStore.userInfo?.avatar ? undefined : 'UserFilled'"
          />
          <div class="input-right">
            <el-input
                v-model="content"
                type="textarea"
                :rows="4"
                :maxlength="500"
                show-word-limit
                placeholder="欢迎留言发表您的看法！GNS不会基于政治观点移除任何评论，但请不要发布含有人身攻击或广告内容。"
                resize="none" />
            <div class="input-footer">
              <span class="community-guidelines">请注意，发表评论时应当始终做到尊重他人并遵守GNS的社区准则。</span>
              <el-button
                  type="primary"
                  class="submit-btn"
                  :loading="submitting"
                  :disabled="!content.trim()"
                  @click="submitComment(null)">
                发表评论
              </el-button>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="login-tip">
          <div class="login-tip-content">
            <el-icon class="login-icon"><ChatDotRound /></el-icon>
            <span>加入讨论，分享您的独到见解</span>
          </div>
          <router-link to="/login">
            <el-button type="primary" round>立即登录 / 注册</el-button>
          </router-link>
        </div>
      </template>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton v-for="i in 3" :key="i" :rows="3" animated style="margin-bottom:20px" />
    </div>

    <!-- 评论列表 -->
    <div v-else-if="comments.length === 0" class="empty">
      暂无评论
    </div>

    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">

        <!-- 顶级评论 -->
        <el-avatar
            :size="38"
            :src="comment.avatar || ''"
            :icon="comment.avatar ? undefined : 'UserFilled'"
            class="avatar"
        />
        <div class="comment-body">

          <!-- 评论头部 -->
          <div class="comment-header">
            <span class="username">{{ comment.username }}</span>
            <span class="time">{{ formatTime(comment.createdAt) }}</span>
          </div>

          <!-- 评论内容 -->
          <div class="comment-content">{{ comment.content }}</div>

          <!-- 操作栏 -->
          <div class="comment-actions">
            <!-- 点赞 -->
            <span
                class="action-btn"
                :class="{ liked: comment.liked }"
                @click="handleLike(comment)">
              <svg class="like-icon" viewBox="0 0 24 24" width="15" height="15" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
              </svg>
              <span class="like-count">{{ comment.likeCount || 0 }}</span>
            </span>

            <!-- 回复 -->
            <span
                v-if="userStore.isLoggedIn"
                class="action-btn"
                @click="toggleReply(comment.id)">
              回复
            </span>

            <!-- 删除 -->
            <span
                v-if="canDelete(comment)"
                class="action-btn danger"
                @click="handleDelete(comment.id)">
              删除
            </span>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyingTo === comment.id" class="reply-input-wrap">
            <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :maxlength="500"
                :placeholder="`回复 ${comment.username}...`"
                resize="none" />
            <div class="reply-input-footer">
              <el-button size="small" @click="replyingTo = null">取消</el-button>
              <el-button
                  type="primary"
                  size="small"
                  :loading="replySubmitting"
                  :disabled="!replyContent.trim()"
                  @click="submitComment(comment.id)">
                发布回复
              </el-button>
            </div>
          </div>

          <!-- 子回复列表 -->
          <div v-if="comment.replies && comment.replies.length > 0" class="replies">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <el-avatar
                  :size="28"
                  :src="reply.avatar || ''"
                  :icon="reply.avatar ? undefined : 'UserFilled'"
                  class="reply-avatar"
              />
              <div class="reply-body">
                <div class="comment-header">
                  <span class="username">{{ reply.username }}</span>
                  <span class="time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ reply.content }}</div>
                <div class="comment-actions">
                  <span
                      class="action-btn"
                      :class="{ liked: reply.liked }"
                      @click="handleLike(reply)">
                    <svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
                    </svg>
                    <span class="like-count">{{ reply.likeCount || 0 }}</span>
                  </span>
                  <span
                      v-if="canDelete(reply)"
                      class="action-btn danger"
                      @click="handleDelete(reply.id)">
                    删除
                  </span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getCommentList, getCommentCount, addComment, likeComment, deleteComment } from '../api/comment'

const props = defineProps({
  articleId: { type: [Number, String], required: true }
})

const userStore = useUserStore()
const comments = ref([])
const total = ref(0)
const loading = ref(false)

// 发评论
const content = ref('')
const submitting = ref(false)

// 回复
const replyingTo = ref(null)  // 当前正在回复的评论ID
const replyContent = ref('')
const replySubmitting = ref(false)

onMounted(loadComments)

async function loadComments() {
  loading.value = true
  try {
    const [listRes, countRes] = await Promise.all([
      getCommentList(props.articleId),
      getCommentCount(props.articleId)
    ])
    comments.value = listRes.data
    total.value = countRes.data
  } finally {
    loading.value = false
  }
}

// 发表评论或回复
async function submitComment(parentId) {
  const isReply = parentId !== null
  const text = isReply ? replyContent.value : content.value
  if (!text.trim()) return

  if (isReply) replySubmitting.value = true
  else submitting.value = true

  try {
    await addComment({
      articleId: Number(props.articleId),
      parentId: parentId,
      content: text.trim()
    })
    ElMessage.success(isReply ? '回复成功' : '评论成功')

    // 清空输入
    if (isReply) {
      replyContent.value = ''
      replyingTo.value = null
    } else {
      content.value = ''
    }

    await loadComments()
  } finally {
    submitting.value = false
    replySubmitting.value = false
  }
}

// 切换回复框（同一评论再点则关闭）
function toggleReply(commentId) {
  if (replyingTo.value === commentId) {
    replyingTo.value = null
    replyContent.value = ''
  } else {
    replyingTo.value = commentId
    replyContent.value = ''
  }
}

// 点赞
async function handleLike(comment) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  try {
    const res = await likeComment(comment.id)
    // 乐观更新，不重新请求整个列表
    comment.liked = res.data
    comment.likeCount = res.data
        ? (comment.likeCount || 0) + 1
        : Math.max(0, (comment.likeCount || 0) - 1)
  } catch (e) {
    // 失败时刷新
    await loadComments()
  }
}

// 删除
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

// 判断是否有权限删除
function canDelete(comment) {
  if (!userStore.isLoggedIn) return false
  if (userStore.isAdmin) return true
  return userStore.userInfo?.userId === comment.userId
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = (now - d) / 1000 / 60
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${Math.floor(diff)}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  if (diff < 10080) return `${Math.floor(diff / 1440)}天前`
  // 超过一周显示完整日期时间
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.comment-section { margin-top: 48px; padding-top: 32px; border-top: 2px solid #f1f5f9; }

.section-title {
  font-size: 20px; font-weight: 700; color: #1e293b;
  margin-bottom: 24px; display: flex; align-items: center; gap: 8px;
}
.count {
  background: #f1f5f9; color: #64748b;
  font-size: 14px; font-weight: 500;
  padding: 2px 10px; border-radius: 20px;
}

/* 发评论框 */
.comment-input-wrap {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}
.comment-input-wrap:focus-within {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}
.input-row { display: flex; gap: 16px; align-items: flex-start; }
.input-right { flex: 1; }

:deep(.el-textarea__inner) {
  background-color: #f8fafc;
  border: none;
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  color: #1e293b;
  transition: all 0.3s;
  box-shadow: none !important;
}
:deep(.el-textarea__inner:focus) {
  background-color: #ffffff;
  box-shadow: inset 0 0 0 1px #3b82f6 !important;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
.community-guidelines {
  font-size: 12px;
  color: #94a3b8;
  max-width: 65%;
  line-height: 1.5;
}
.submit-btn {
  border-radius: 20px;
  padding: 0 24px;
  font-weight: 600;
}

.login-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
}
.login-tip-content {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #475569;
  font-size: 15px;
  font-weight: 500;
}
.login-icon {
  font-size: 24px;
  color: #3b82f6;
}

/* 评论列表 */
.comment-list { display: flex; flex-direction: column; }
.comment-item {
  display: flex; gap: 12px;
  padding: 20px 0; border-bottom: 1px solid #f1f5f9;
}
.comment-item:last-child { border-bottom: none; }
.avatar { flex-shrink: 0; margin-top: 2px; }
.comment-body { flex: 1; min-width: 0; }

/* 评论头部 */
.comment-header {
  display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
}
.username { font-size: 14px; font-weight: 600; color: #1e293b; }
.time { font-size: 12px; color: #94a3b8; }

/* 评论内容 */
.comment-content {
  font-size: 15px; color: #334155; line-height: 1.7;
  margin-bottom: 10px; word-break: break-word;
}

/* 操作栏 */
.comment-actions { display: flex; align-items: center; gap: 16px; }
.action-btn {
  font-size: 13px; color: #94a3b8; cursor: pointer;
  display: flex; align-items: center; gap: 4px;
  transition: all .15s; user-select: none;
}
.action-btn:hover { color: #64748b; }
.action-btn:hover .like-icon { transform: translateY(-1px) scale(1.05); }
.action-btn.liked { color: #3b82f6; }
.action-btn.liked .like-icon { fill: #3b82f6; stroke: #3b82f6; }
.like-icon { transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.action-btn.danger:hover { color: #ef4444; }
.like-count { font-size: 13px; }

/* 回复输入框 */
.reply-input-wrap {
  margin-top: 12px; background: #f8fafc;
  border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px;
}
.reply-input-footer {
  display: flex; justify-content: flex-end;
  gap: 8px; margin-top: 8px;
}

/* 子回复 */
.replies {
  margin-top: 12px; background: #f8fafc;
  border-radius: 8px; padding: 12px;
  display: flex; flex-direction: column; gap: 0;
}
.reply-item {
  display: flex; gap: 10px; align-items: flex-start;
  padding: 10px 0; border-bottom: 1px solid #f1f5f9;
}
.reply-item:last-child { border-bottom: none; padding-bottom: 0; }
.reply-item:first-child { padding-top: 0; }
.reply-avatar { flex-shrink: 0; }
.reply-body { flex: 1; min-width: 0; }
.reply-body .comment-content { font-size: 14px; margin-bottom: 6px; }
.reply-body .comment-actions { gap: 12px; }

/* 其他 */
.empty { text-align: center; color: #94a3b8; padding: 40px 0; font-size: 14px; }
.loading-wrap { padding: 16px 0; }
</style>