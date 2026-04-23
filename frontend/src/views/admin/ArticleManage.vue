<template>
  <div class="page">
    <div class="header">
      <h2>文章管理</h2>
      <div style="display:flex;gap:8px">
        <el-popconfirm
            title="确认删除全部文章？此操作不可恢复！"
            confirm-button-text="确认删除"
            cancel-button-text="取消"
            confirm-button-type="danger"
            width="260"
            @confirm="handleDeleteAll">
          <template #reference>
            <el-button type="danger" plain>🗑 清空所有文章</el-button>
          </template>
        </el-popconfirm>
        <el-button type="primary" @click="openDialog()">+ 新增文章</el-button>
      </div>
    </div>

    <el-table :data="articles" stripe v-loading="loading">
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="source" label="来源" width="160" />
      <el-table-column prop="viewCount" label="阅读" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="currentPage" :page-size="15"
                     :total="total" layout="prev, pager, next" @current-change="loadData" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑文章' : '新增文章'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="form.source" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImg" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminArticleList, createArticle, updateArticle,
  deleteArticle, updateArticleStatus, deleteAllArticles } from '../../api/admin'

const articles = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const form = ref({ title: '', source: '', summary: '', coverImg: '', content: '' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminArticleList({ page: currentPage.value, size: 15 })
    articles.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row = null) {
  editingId.value = row?.id || null
  form.value = row
      ? { title: row.title, source: row.source, summary: row.summary, coverImg: row.coverImg, content: row.content }
      : { title: '', source: '', summary: '', coverImg: '', content: '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateArticle(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createArticle(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  await deleteArticle(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleDeleteAll() {
  await deleteAllArticles()
  ElMessage.success('已清空所有文章')
  currentPage.value = 1
  loadData()
}

async function toggleStatus(row) {
  await updateArticleStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  loadData()
}
</script>

<style scoped>
.page { padding: 32px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
h2 { margin: 0; color: #1e293b; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>