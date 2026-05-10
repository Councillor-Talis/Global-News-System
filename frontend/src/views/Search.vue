<template>
  <div class="page">
    <NavBar :categories="[]" :showHot="false" :showSearchInput="false" />
    <div class="container">

      <!-- 搜索头部 -->
      <div class="search-header">
        <div class="google-search-container">
          <div class="google-search-box" :class="{ 'is-focused': isFocused }">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              type="text"
              v-model="keyword"
              placeholder="搜索全球新闻..."
              @keyup.enter="doSearch"
              @focus="isFocused = true"
              @blur="isFocused = false"
            />
            <span v-if="keyword" class="clear-icon" @mousedown.prevent="clearSearch">×</span>
          </div>
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

const isFocused = ref(false)
function clearSearch() {
  keyword.value = ''
}

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

  router.replace({ path: '/search', query: { keyword: kw } })

  loading.value = true
  searched.value = false
  try {
    const res = await searchNews({ keyword: kw, page: currentPage.value, size: 9 })
    // ES 返回 { total, page, size, records }
    // MyBatis 返回 { total, records, current, size }
    // 两种格式都兼容
    const data = res.data
    articles.value = data.records || []
    total.value = data.total || 0
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
.search-header { margin-bottom: 40px; }

.google-search-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.google-search-box {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 680px;
  height: 52px;
  background: #ffffff;
  border: 1px solid #dfe1e5;
  border-radius: 26px;
  padding: 0 20px;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.google-search-box:hover,
.google-search-box.is-focused {
  background: #ffffff;
  box-shadow: 0 1px 6px rgba(32,33,36,0.28);
  border-color: rgba(223,225,229,0);
}
.search-icon {
  font-size: 20px;
  color: #9aa0a6;
  margin-right: 12px;
}
.google-search-box input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: #202124;
}
.google-search-box input::placeholder {
  color: #9aa0a6;
}
.clear-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  font-size: 20px;
  color: #9aa0a6;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s;
}
.clear-icon:hover {
  background: #f1f3f4;
  color: #5f6368;
}

.result-info { text-align: center; font-size: 14px; color: #64748b; }
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