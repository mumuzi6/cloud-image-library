<template>
  <div id="spaceManagePage">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h2 class="page-title">空间管理</h2>
            <p class="page-subtitle">管理系统中的所有空间资源</p>
          </div>
          <div class="action-section">
            <a-space>
              <a-button type="primary" href="/add_space" target="_blank" class="action-button">
                ➕ 创建空间
              </a-button>
              <a-button type="primary" ghost href="/space_analyze?queryPublic=1" target="_blank" class="action-button-ghost">
                📊 分析公共图库
              </a-button>
              <a-button type="primary" ghost href="/space_analyze?queryAll=1" target="_blank" class="action-button-ghost">
                📈 分析全部空间
              </a-button>
            </a-space>
          </div>
        </div>
      </div>

      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-card">
          <h3 class="section-title">筛选条件</h3>
          <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
            <a-form-item label="空间名称">
              <a-input 
                v-model:value="searchParams.spaceName" 
                placeholder="请输入空间名称" 
                allow-clear 
                class="modern-input"
              />
            </a-form-item>
            <a-form-item name="spaceLevel" label="空间级别">
              <a-select
                v-model:value="searchParams.spaceLevel"
                style="min-width: 180px"
                placeholder="请选择空间级别"
                :options="SPACE_LEVEL_OPTIONS"
                allow-clear
                class="modern-select"
              />
            </a-form-item>
            <a-form-item label="空间类别" name="spaceType">
              <a-select
                v-model:value="searchParams.spaceType"
                :options="SPACE_TYPE_OPTIONS"
                placeholder="请输入空间类别"
                style="min-width: 180px"
                allow-clear
                class="modern-select"
              />
            </a-form-item>
            <a-form-item label="用户ID">
              <a-input 
                v-model:value="searchParams.userId" 
                placeholder="请输入用户ID" 
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
            <h3 class="section-title">空间列表</h3>
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
              <template v-if="column.dataIndex === 'spaceLevel'">
                <a-tag color="blue" class="level-tag">
                  💎 {{ SPACE_LEVEL_MAP[record.spaceLevel as keyof typeof SPACE_LEVEL_MAP] }}
                </a-tag>
              </template>
              <!-- 空间类别 -->
              <template v-if="column.dataIndex === 'spaceType'">
                <a-tag 
                  :color="record.spaceType === 0 ? 'green' : record.spaceType === 1 ? 'purple' : 'orange'"
                  class="type-tag"
                >
                  {{ record.spaceType === 0 ? '🌍 公共' : record.spaceType === 1 ? '🔒 私有' : '👥 团队' }}
                </a-tag>
              </template>
              <template v-if="column.dataIndex === 'spaceUseInfo'">
                <div class="usage-cell">
                  <div class="usage-item">
                    <span class="usage-label">💾 容量：</span>
                    <span class="usage-value">{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</span>
                  </div>
                  <div class="usage-item">
                    <span class="usage-label">📁 数量：</span>
                    <span class="usage-value">{{ record.totalCount }} / {{ record.maxCount }}</span>
                  </div>
                  <div class="usage-progress">
                    <a-progress 
                      :percent="Math.round((record.totalSize / record.maxSize) * 100)" 
                      size="small"
                      :stroke-color="{ '0%': '#667eea', '100%': '#764ba2' }"
                    />
                  </div>
                </div>
              </template>
              <template v-if="column.dataIndex === 'createTime'">
                <span class="time-text">
                  {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                </span>
              </template>
              <template v-if="column.dataIndex === 'editTime'">
                <span class="time-text">
                  {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
                </span>
              </template>
              <template v-else-if="column.key === 'action'">
                <div class="action-cell">
                  <a-space wrap>
                    <a-button 
                      type="link" 
                      :href="`/space_analyze?spaceId=${record.id}`" 
                      target="_blank"
                      class="analyze-button"
                    >
                      📊 分析
                    </a-button>
                    <a-button 
                      type="link" 
                      :href="`/add_space?id=${record.id}`" 
                      target="_blank"
                      class="edit-button"
                    >
                      ✏️ 编辑
                    </a-button>
                    <a-button danger class="delete-button" @click="doDelete(record.id)">
                      🗑️ 删除
                    </a-button>
                  </a-space>
                </div>
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
import { deleteSpaceUsingPost, listSpaceByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SPACE_LEVEL_MAP,
  SPACE_LEVEL_OPTIONS,
  SPACE_TYPE_MAP,
  SPACE_TYPE_OPTIONS,
} from '../../constants/space.ts'
import { formatSize } from '../../utils'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
    width: 150,
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
    width: 120,
  },
  {
    title: '空间类别',
    dataIndex: 'spaceType',
    width: 120,
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
    width: 200,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 250,
    fixed: 'right',
  },
]

// 定义数据
const dataList = ref<API.Space[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
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
  const res = await deleteSpaceUsingPost({ id: Number(id) })
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
#spaceManagePage {
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
  margin-bottom: 32px;
  padding: 32px 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.title-section {
  text-align: left;
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

.action-section {
  flex-shrink: 0;
}

.action-button {
  border-radius: 8px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.action-button-ghost {
  border-radius: 8px;
  border: 2px solid #667eea;
  color: #667eea;
  font-weight: 600;
  transition: all 0.3s ease;
}

.action-button-ghost:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
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

.modern-select :deep(.ant-select-selector) {
  border-radius: 8px !important;
  border: 2px solid #e1e8ed !important;
  background: rgba(248, 250, 252, 0.8) !important;
  transition: all 0.3s ease;
}

.modern-select :deep(.ant-select-focused .ant-select-selector) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
  background: white !important;
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
.level-tag,
.type-tag {
  border-radius: 20px;
  font-weight: 500;
  padding: 4px 12px;
  border: none;
}

.usage-cell {
  font-size: 12px;
}

.usage-item {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.usage-label {
  color: #6b7280;
  font-weight: 500;
}

.usage-value {
  color: #374151;
  font-weight: 600;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.usage-progress {
  margin-top: 8px;
}

.time-text {
  color: #6b7280;
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.action-cell {
  min-width: 240px;
}

.analyze-button,
.edit-button {
  color: #10b981;
  font-weight: 500;
  transition: all 0.3s ease;
}

.analyze-button:hover,
.edit-button:hover {
  color: #059669;
  transform: translateY(-1px);
}

.delete-button {
  border-radius: 6px;
  border: 1px solid #ef4444;
  color: #ef4444;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 12px;
  padding: 4px 8px;
  height: auto;
}

.delete-button:hover {
  background: #ef4444;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  #spaceManagePage {
    padding: 16px;
  }
  
  .page-container {
    max-width: 100%;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .title-section {
    text-align: center;
    width: 100%;
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
  
  .modern-table {
    overflow-x: auto;
  }
}
</style>
