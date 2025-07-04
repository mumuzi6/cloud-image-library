<template>
  <div id="pictureManagePage">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <div class="title-section">
            <h2 class="page-title">图片管理</h2>
            <p class="page-subtitle">管理系统中的所有图片资源</p>
          </div>
          <div class="action-section">
            <a-space>
              <a-button type="primary" href="/add_picture" target="_blank" class="action-button">
                ➕ 创建图片
              </a-button>
              <a-button type="primary" href="/add_picture/batch" target="_blank" ghost class="action-button-ghost">
                📦 批量创建图片
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
            <a-form-item label="关键词">
              <a-input
                v-model:value="searchParams.searchText"
                placeholder="从名称和简介搜索"
                allow-clear
                class="modern-input"
              />
            </a-form-item>
            <a-form-item label="类型">
              <a-input 
                v-model:value="searchParams.category" 
                placeholder="请输入类型" 
                allow-clear 
                class="modern-input"
              />
            </a-form-item>
            <a-form-item label="标签">
              <a-select
                v-model:value="searchParams.tags"
                mode="tags"
                placeholder="请输入标签"
                style="min-width: 180px"
                allow-clear
                class="modern-select"
              />
            </a-form-item>
            <a-form-item name="reviewStatus" label="审核状态">
              <a-select
                v-model:value="searchParams.reviewStatus"
                style="min-width: 180px"
                placeholder="请选择审核状态"
                :options="PIC_REVIEW_STATUS_OPTIONS"
                allow-clear
                class="modern-select"
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
            <h3 class="section-title">图片列表</h3>
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
            :scroll="{ x: 1600, y: 600 }"
            size="middle"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'url'">
                <div class="image-cell">
                  <a-image :src="record.url" :width="80" :height="80" class="picture-thumbnail" />
                </div>
              </template>
              <template v-if="column.dataIndex === 'tags'">
                <div class="tags-cell">
                  <a-space wrap>
                    <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag" class="modern-tag">
                      {{ tag }}
                    </a-tag>
                  </a-space>
                </div>
              </template>
              <template v-if="column.dataIndex === 'picInfo'">
                <div class="info-cell">
                  <div class="info-item">📐 {{ record.picFormat }}</div>
                  <div class="info-item">📏 {{ record.picWidth }}×{{ record.picHeight }}</div>
                  <div class="info-item">📊 {{ record.picScale }}</div>
                  <div class="info-item">💾 {{ (record.picSize / 1024).toFixed(2) }}KB</div>
                </div>
              </template>
              <template v-if="column.dataIndex === 'reviewMessage'">
                <div class="review-cell">
                  <div class="review-status">
                                         <a-tag 
                       :color="record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? 'success' : 
                              record.reviewStatus === PIC_REVIEW_STATUS_ENUM.REJECT ? 'error' : 'warning'"
                       class="status-tag"
                     >
                       {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus as keyof typeof PIC_REVIEW_STATUS_MAP] }}
                     </a-tag>
                  </div>
                  <div v-if="record.reviewMessage" class="review-message">{{ record.reviewMessage }}</div>
                  <div v-if="record.reviewerId" class="review-info">审核人：{{ record.reviewerId }}</div>
                  <div v-if="record.reviewTime" class="review-info">
                    {{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}
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
                      v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
                      type="link"
                      class="approve-button"
                      @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
                    >
                      ✅ 通过
                    </a-button>
                    <a-button
                      v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
                      type="link"
                      danger
                      class="reject-button"
                      @click="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
                    >
                      ❌ 拒绝
                    </a-button>
                    <a-button 
                      type="link" 
                      :href="`/add_picture?id=${record.id}`" 
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
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  listPictureByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '../../constants/picture.ts'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 70,
    align: 'center',
  },
  {
    title: '图片',
    dataIndex: 'url',
    width: 100,
    align: 'center',
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 120,
    ellipsis: true,
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
    width: 180,
  },
  {
    title: '类型',
    dataIndex: 'category',
    width: 90,
    align: 'center',
  },
  {
    title: '标签',
    dataIndex: 'tags',
    width: 140,
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
    width: 140,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 80,
    align: 'center',
  },
  {
    title: '空间ID',
    dataIndex: 'spaceId',
    width: 80,
    align: 'center',
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
    width: 220,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 140,
    align: 'center',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
    width: 140,
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    width: 280,
    fixed: 'right',
    align: 'center',
  },
]

// 定义数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
    nullSpaceId: true,
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
  const res = await deletePictureUsingPost({ id: Number(id) })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 审核图片
const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage =
    reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员操作通过' : '管理员操作拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    // 重新获取列表数据
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}
</script>

<style scoped>
#pictureManagePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.page-container {
  max-width: 1600px;
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

.modern-table :deep(.ant-table-container) {
  border-radius: 12px;
}

.modern-table :deep(.ant-table-body) {
  overflow-x: auto !important;
  overflow-y: auto !important;
}

.modern-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border: none;
  font-weight: 600;
  color: #374151;
  padding: 18px 8px;
  height: 60px;
  vertical-align: middle;
  text-align: center;
  font-size: 14px;
  line-height: 1.4;
}

.modern-table :deep(.ant-table-tbody > tr > td) {
  border: none;
  padding: 20px 8px;
  border-bottom: 1px solid rgba(230, 235, 241, 0.5);
  vertical-align: middle;
  min-height: 80px;
  height: auto;
  line-height: 1.5;
  font-size: 13px;
}

.modern-table :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(102, 126, 234, 0.05);
}

/* 单元格样式 */
.image-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.picture-thumbnail {
  border-radius: 8px;
  object-fit: cover;
  border: 2px solid #e1e8ed;
  transition: all 0.3s ease;
}

.picture-thumbnail:hover {
  transform: scale(1.05);
  border-color: #667eea;
}

.tags-cell {
  max-width: 140px;
  overflow: hidden;
  word-wrap: break-word;
}

.modern-tag {
  border-radius: 12px;
  font-size: 11px;
  margin: 2px 1px;
  padding: 2px 6px;
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  display: inline-block;
  line-height: 1.3;
}

.info-cell {
  font-size: 11px;
  color: #6b7280;
  line-height: 1.4;
  word-wrap: break-word;
  overflow: hidden;
}

.info-item {
  margin-bottom: 6px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 1px 0;
}

.review-cell {
  max-width: 220px;
  overflow: hidden;
  word-wrap: break-word;
}

.review-status {
  margin-bottom: 6px;
}

.status-tag {
  border-radius: 12px;
  font-weight: 500;
  font-size: 11px;
  padding: 2px 8px;
}

.review-message,
.review-info {
  font-size: 11px;
  color: #6b7280;
  margin-bottom: 4px;
  line-height: 1.4;
  word-wrap: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  max-height: 2.8em;
}

.time-text {
  color: #6b7280;
  font-size: 11px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  line-height: 1.4;
}

.action-cell {
  min-width: 280px;
  text-align: center;
}

.approve-button,
.edit-button {
  color: #10b981;
  font-weight: 500;
  transition: all 0.3s ease;
}

.approve-button:hover,
.edit-button:hover {
  color: #059669;
  transform: translateY(-1px);
}

.reject-button {
  color: #ef4444;
  font-weight: 500;
  transition: all 0.3s ease;
}

.reject-button:hover {
  color: #dc2626;
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
  #pictureManagePage {
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
