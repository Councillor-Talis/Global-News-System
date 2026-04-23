<template>
  <div class="page">
    <NavBar :categories="[]" />
    <div class="container">

      <!-- 搜索头部 -->
      <div class="search-header">
        <div class="search-bar">
          <el-input
              v-model="keyword"
              placeholder="搜索新闻..."
              size="large"
              clearable
              @keyup.enter="doSearch"
              @clear="doSearch"
              style="max-width: 560px">
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append>
              <el-button type="primary" @click="doSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="result-info" v-if="!loading && searched">
          找到 <strong>{{ total }}</strong> 条与
          "<strong>{{ lastKeyword }}</strong>" 相关的新闻
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="grid">
        <el-skeleton v-for="i in 9" :key="i" :rows="4" animated style="margin-bottom:8px" />
      </div>

      <!-- 结果列表 -->
      <div v-else-if="articles.length > 0" class="grid">
        <NewsCard
            v-for="article in articles" :key="article.id"
            :article="article"
            :keyword="lastKeyword"
            :categoryName="getCategoryName(article.categoryId)" />
      </div>

      <!-- 空结果 -->
      <el-empty
          v-else-if="searched"
          :description="`没有找到与'${lastKeyword}'相关的新闻`"
      style="margin-top: 60px" />

      <!-- 初始状态 -->
      <div v-else class="hint">输入关键词搜索新闻</div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="9"
            :total="total"
            layout="prev, pager, next"
            @current-change="doSearch" />
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import NewsCard from '../components/NewsCard.vue'
import { searchNews, getCategoryList } from '../api/news'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const lastKeyword = ref('')
const articles = ref([])
const categories = ref([])
const loading = ref(false)
const searched = ref(false)
const total = ref(0)
const currentPage = ref(1)

onMounted(async () => {
  const res = await getCategoryList()
  categories.value = res.data
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    await doSearch()
  }
})

// URL 参数变化时重新搜索
watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val
    currentPage.value = 1
    doSearch()
  }
})

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return

  // 更新 URL
  router.replace({ path: '/search', query: { keyword: kw } })

  loading.value = true
  searched.value = false
  try {
    const res = await searchNews({ keyword: kw, page: currentPage.value, size: 9 })
    articles.value = res.data.records
    total.value = res.data.total
    lastKeyword.value = kw
    searched.value = true
  } finally {
    loading.value = false
  }
}

function getCategoryName(categoryId) {
  return categories.value.find(c => c.id === categoryId)?.name || ''
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f8fafc; }
.container { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
.search-header { margin-bottom: 32px; }
.search-bar { margin-bottom: 16px; }
.result-info { font-size: 14px; color: #64748b; }
.result-info strong { color: #1e293b; }
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.pagination { display: flex; justify-content: center; margin-top: 40px; }
.hint { text-align: center; color: #94a3b8; padding: 80px 0; font-size: 16px; }
@media (max-width: 900px) { .grid { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 600px) { .grid { grid-template-columns: 1fr; } }
</style>