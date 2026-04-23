<template>
  <div class="page">
    <div class="header">
      <h2>用户管理</h2>
      <span class="total">共 {{ total }} 名用户</span>
    </div>

    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'warning' : 'info'" size="small">
            {{ row.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-popconfirm
              :title="row.status === 1 ? '确认禁用该用户？' : '确认启用该用户？'"
              @confirm="toggleStatus(row)">
            <template #reference>
              <el-button
                  size="small"
                  :type="row.status === 1 ? 'danger' : 'success'"
                  :disabled="row.role === 1">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
          v-model:current-page="currentPage"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, updateUserStatus } from '../../api/admin'

const users = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList({ page: currentPage.value, size: 10 })
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row) {
  await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  loadData()
}
</script>

<style scoped>
.page { padding: 32px; }
.header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
h2 { margin: 0; color: #1e293b; }
.total { font-size: 14px; color: #64748b; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>