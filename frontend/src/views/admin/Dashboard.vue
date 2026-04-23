<template>
  <div class="dashboard">
    <h2>数据概览</h2>

    <div class="stats" v-loading="statsLoading">
      <div class="stat-card">
        <div class="label">文章总数</div>
        <div class="value">{{ stats.totalArticles ?? '-' }}</div>
      </div>
      <div class="stat-card">
        <div class="label">用户总数</div>
        <div class="value">{{ stats.totalUsers ?? '-' }}</div>
      </div>
      <div class="stat-card">
        <div class="label">今日新增文章</div>
        <div class="value accent">{{ stats.todayArticles ?? '-' }}</div>
      </div>
      <div class="stat-card">
        <div class="label">总阅读量</div>
        <div class="value">{{ formatNum(stats.totalViews) }}</div>
      </div>
    </div>

    <div class="section-header">
      <h3>最新文章</h3>
      <div style="display:flex;gap:8px">
        <el-button size="small" type="warning" :loading="crawling" @click="triggerCrawl">
          🔄 手动采集
        </el-button>
        <el-button size="small" @click="$router.push('/admin/articles')">查看全部</el-button>
      </div>
    </div>

    <el-table :data="articles" stripe v-loading="tableLoading">
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="source" label="来源" width="110" />
      <el-table-column prop="viewCount" label="阅读量" width="90" />
      <el-table-column prop="pubTime" label="发布时间" width="170" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStats, getAdminArticleList, triggerCrawl as triggerCrawlApi } from '../../api/admin'

const stats = ref({})
const articles = ref([])
const statsLoading = ref(false)
const tableLoading = ref(false)
const crawling = ref(false)

onMounted(async () => {
  statsLoading.value = true
  tableLoading.value = true
  try {
    const [statsRes, listRes] = await Promise.all([
      getStats(),
      getAdminArticleList({ page: 1, size: 8 })
    ])
    stats.value = statsRes.data
    articles.value = listRes.data.records
  } finally {
    statsLoading.value = false
    tableLoading.value = false
  }
})

async function triggerCrawl() {
  crawling.value = true
  try {
    const res = await triggerCrawlApi()
    ElMessage.success(res.data)
    const [statsRes, listRes] = await Promise.all([
      getStats(),
      getAdminArticleList({ page: 1, size: 8 })
    ])
    stats.value = statsRes.data
    articles.value = listRes.data.records
  } finally {
    crawling.value = false
  }
}

function formatNum(n) {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return n.toString()
}
</script>

<style scoped>
.dashboard { padding: 32px; }
h2 { margin: 0 0 20px; color: #1e293b; }
h3 { margin: 0; color: #1e293b; }
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 32px; }
.stat-card {
  background: #fff; border-radius: 12px;
  padding: 20px 24px; border: 1px solid #e2e8f0;
}
.label { font-size: 13px; color: #64748b; margin-bottom: 10px; }
.value { font-size: 36px; font-weight: 700; color: #1e293b; }
.value.accent { color: #3b82f6; }
.section-header {
  display: flex; justify-content: space-between;
  align-items: center; margin-bottom: 16px;
}
@media (max-width: 900px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>