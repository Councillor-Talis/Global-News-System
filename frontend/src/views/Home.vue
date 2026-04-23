<template>
  <div class="home">
    <NavBar :categories="categories" :activeCategoryId="activeCategoryId"
            @categoryChange="onCategoryChange" @hotModeChange="onHotModeChange" />

    <div class="container">
      <!-- 加载中 -->
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated v-for="i in 6" :key="i" style="margin-bottom:20px" />
      </div>

      <!-- 新闻网格 -->
      <div v-else class="grid">
        <NewsCard v-for="article in articles" :key="article.id"
                  :article="article" :categoryName="getCategoryName(article.categoryId)" />
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && articles.length === 0" description="暂无新闻" />

      <!-- 分页 -->
      <div class="pagination" v-if="!isHotMode && total > 0">
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadNews"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '../components/NavBar.vue'
import NewsCard from '../components/NewsCard.vue'
import { getNewsList, getCategoryList, getHotNews } from '../api/news'

const categories = ref([])
const articles = ref([])
const activeCategoryId = ref(null)
const isHotMode = ref(false)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(9)
const total = ref(0)

onMounted(async () => {
  // 加载分类
  const res = await getCategoryList()
  categories.value = res.data
  // 加载新闻
  await loadNews()
})

async function loadNews() {
  loading.value = true
  try {
    const res = await getNewsList({
      page: currentPage.value,
      size: pageSize.value,
      categoryId: activeCategoryId.value === null || activeCategoryId.value === categories.value[0]?.id
          ? null : activeCategoryId.value,
    })
    articles.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onCategoryChange(id) {
  activeCategoryId.value = id
  isHotMode.value = false
  currentPage.value = 1
  loadNews()
}

async function onHotModeChange(active) {
  isHotMode.value = active
  if (active) {
    loading.value = true
    try {
      const res = await getHotNews()
      articles.value = res.data
      total.value = 0
    } finally {
      loading.value = false
    }
  } else {
    await loadNews()
  }
}

function getCategoryName(categoryId) {
  return categories.value.find(c => c.id === categoryId)?.name || ''
}
</script>

<style scoped>
.home { min-height: 100vh; background: #f8fafc; }
.container { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.pagination { display: flex; justify-content: center; margin-top: 40px; }
.loading { display: grid; grid-template-columns: repeat(3,1fr); gap: 24px; }
@media (max-width: 900px) { .grid { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 600px) { .grid { grid-template-columns: 1fr; } }
</style>