<template>
  <div id="userManagePage">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理系统中的所有用户信息</p>
      </div>

      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-card">
          <h3 class="section-title">筛选条件</h3>
          <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
            <a-form-item label="账号">
              <a-input 
                v-model:value="searchParams.userAccount" 
                placeholder="输入账号" 
                allow-clear 
                class="modern-input"
              />
            </a-form-item>
            <a-form-item label="用户名">
              <a-input 
                v-model:value="searchParams.userName" 
                placeholder="输入用户名" 
                allow-clear 
                class="modern-input"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" html-type="submit" class="search-button">
                🔍 搜索
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 数据表格区域 -->
      <div class="table-section">
        <div class="table-card">
          <div class="table-header">
            <h3 class="section-title">用户列表</h3>
            <div class="table-info">
              共 {{ total }} 条记录
            </div>
          </div>
          <a-table
            :columns="columns"
            :data-source="dataList"
            :pagination="pagination"
            @change="doTableChange"
            class="modern-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'userAvatar'">
                <div class="avatar-cell">
                  <a-image :src="record.userAvatar" :width="60" :height="60" class="user-avatar" />
                </div>
              </template>
              <template v-else-if="column.dataIndex === 'userRole'">
                <div v-if="record.userRole === 'admin'">
                  <a-tag color="green" class="role-tag">👑 管理员</a-tag>
                </div>
                <div v-else>
                  <a-tag color="blue" class="role-tag">👤 普通用户</a-tag>
                </div>
              </template>
              <template v-if="column.dataIndex === 'createTime'">
                <span class="time-text">
                  {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button danger class="delete-button" @click="doDelete(record.id)">
                  🗑️ 删除
                </a-button>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUserUsingPost, listUserVoByPageUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 100,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 100,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    ellipsis: true,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
  },
]

// 定义数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({ id: Number(id) })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>
#userManagePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  padding: 32px 0;
}

.page-title {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  text-shadow: none;
}

.page-subtitle {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 24px;
}

.search-card,
.table-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.search-card:hover,
.table-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表单样式 */
.search-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.search-form :deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #374151;
}

.modern-input :deep(.ant-input) {
  border-radius: 8px;
  border: 2px solid #e1e8ed;
  padding: 8px 12px;
  transition: all 0.3s ease;
  background: rgba(248, 250, 252, 0.8);
}

.modern-input :deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.search-button {
  border-radius: 8px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
}

/* 表格区域 */
.table-section {
  margin-bottom: 32px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-info {
  color: #6b7280;
  font-size: 14px;
  background: rgba(102, 126, 234, 0.1);
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 500;
}

/* 表格样式 */
.modern-table :deep(.ant-table) {
  border-radius: 12px;
  overflow: hidden;
}

.modern-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border: none;
  font-weight: 600;
  color: #374151;
  padding: 16px 12px;
}

.modern-table :deep(.ant-table-tbody > tr > td) {
  border: none;
  padding: 16px 12px;
  border-bottom: 1px solid rgba(230, 235, 241, 0.5);
}

.modern-table :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(102, 126, 234, 0.05);
}

/* 单元格样式 */
.avatar-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-avatar {
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e1e8ed;
}

.role-tag {
  border-radius: 20px;
  font-weight: 500;
  padding: 4px 12px;
  border: none;
}

.time-text {
  color: #6b7280;
  font-size: 13px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.delete-button {
  border-radius: 8px;
  border: 2px solid #ef4444;
  color: #ef4444;
  font-weight: 600;
  transition: all 0.3s ease;
}

.delete-button:hover {
  background: #ef4444;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

/* 分页样式 */
.modern-table :deep(.ant-pagination) {
  margin-top: 24px;
  text-align: center;
}

.modern-table :deep(.ant-pagination-item) {
  border-radius: 8px;
  border: 2px solid #e1e8ed;
  transition: all 0.3s ease;
}

.modern-table :deep(.ant-pagination-item:hover) {
  border-color: #667eea;
  color: #667eea;
}

.modern-table :deep(.ant-pagination-item-active) {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-color: transparent;
}

.modern-table :deep(.ant-pagination-item-active a) {
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #userManagePage {
    padding: 16px;
  }
  
  .page-container {
    max-width: 100%;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .search-card,
  .table-card {
    padding: 16px;
  }
  
  .search-form {
    display: block;
  }
  
  .search-form :deep(.ant-form-item) {
    display: block;
    margin-bottom: 12px;
  }
  
  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
