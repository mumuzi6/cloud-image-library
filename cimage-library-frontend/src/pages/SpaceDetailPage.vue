<template>
  <div id="spaceDetailPage">
    <!-- 空间信息头部 -->
    <div class="space-header">
      <div class="header-content">
        <div class="space-info">
          <div class="space-title">
            <h1 class="space-name">
              🏠 {{ space.spaceName }}
            </h1>
            <div class="space-type-badge">
              {{ SPACE_TYPE_MAP[space.spaceType] }}
            </div>
          </div>
          <div class="space-stats">
            <div class="stat-item">
              <span class="stat-label">📊 存储使用</span>
              <span class="stat-value">{{ formatSize(space.totalSize) }} / {{ formatSize(space.maxSize) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">📸 图片数量</span>
              <span class="stat-value">{{ total }} 张</span>
            </div>
          </div>
        </div>
        <div class="space-actions">
          <a-button
            v-if="canUploadPicture"
            type="primary"
            :href="`/add_picture?spaceId=${id}`"
            target="_blank"
            class="action-btn primary"
          >
            ➕ 创建图片
          </a-button>
          <a-button
            v-if="canManageSpaceUser"
            type="default"
            :icon="h(TeamOutlined)"
            :href="`/spaceUserManage/${id}`"
            target="_blank"
            class="action-btn"
          >
            成员管理
          </a-button>
          <a-button
            v-if="canManageSpaceUser"
            type="default"
            :icon="h(BarChartOutlined)"
            :href="`/space_analyze?spaceId=${id}`"
            target="_blank"
            class="action-btn"
          >
            空间分析
          </a-button>
          <a-button 
            v-if="canEditPicture" 
            :icon="h(EditOutlined)" 
            @click="doBatchEdit"
            class="action-btn"
          >
            批量编辑
          </a-button>
          <div class="storage-progress">
            <a-tooltip
              :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
            >
              <a-progress
                type="circle"
                :size="56"
                :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
                :strokeColor="{
                  '0%': '#667eea',
                  '100%': '#764ba2',
                }"
              />
            </a-tooltip>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-card">
        <div class="card-header">
          <h3 class="card-title">🔍 图片搜索</h3>
          <p class="card-description">快速找到您需要的图片</p>
        </div>
        <div class="search-content">
          <PictureSearchForm :onSearch="onSearch" />
          <div class="color-search">
            <a-form-item label="🎨 按颜色搜索" class="color-search-item">
              <color-picker format="hex" @pureColorChange="onColorChange" />
            </a-form-item>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片展示区域 -->
    <div class="pictures-section">
      <div class="pictures-card">
        <div class="card-header">
          <h3 class="card-title">📸 图片库</h3>
          <p class="card-description">共 {{ total }} 张图片</p>
        </div>
        <div class="pictures-content">
          <PictureList
            :dataList="dataList"
            :loading="loading"
            :showOp="true"
            :canEdit="canEditPicture"
            :canDelete="canDeletePicture"
            :onReload="fetchData"
          />
        </div>
      </div>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-section">
      <a-pagination
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        @change="onPageChange"
        class="pagination"
        show-size-changer
        show-quick-jumper
        :show-total="(total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`"
      />
    </div>

    <!-- 批量编辑弹窗 -->
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { BarChartOutlined, EditOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '../constants/space.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// -------- 获取空间详情 --------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

// --------- 获取图片列表 --------

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  console.log('new', newSearchParams)

  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  console.log('searchparams', searchParams.value)
  fetchData()
}

// 按照颜色搜索
const onColorChange = async (color: string) => {
  loading.value = true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// ---- 批量编辑图片 -----
const batchEditPictureModalRef = ref()

// 批量编辑图片成功
const onBatchEditPictureSuccess = () => {
  fetchData()
}

// 打开批量编辑图片弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

// 空间 id 改变时，必须重新获取数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)
</script>

<style scoped>
#spaceDetailPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 24px;
}

/* 空间头部 */
.space-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 24px;
}

.space-info {
  flex: 1;
  min-width: 300px;
}

.space-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.space-name {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.space-type-badge {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.space-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.space-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 8px;
  font-weight: 500;
  height: 40px;
  padding: 0 16px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn.primary {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
}

.action-btn.primary:hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.storage-progress {
  margin-left: 16px;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 24px;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  margin-bottom: 20px;
}

.card-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.card-description {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.search-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.color-search {
  width: 100%;
}

.color-search-item {
  margin-bottom: 0;
}

.color-search-item :deep(.ant-form-item-label) {
  font-weight: 500;
  color: #374151;
}

/* 图片展示区域 */
.pictures-section {
  margin-bottom: 24px;
}

.pictures-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.pictures-content {
  margin-top: 20px;
}

/* 分页区域 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.pagination {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.pagination :deep(.ant-pagination-item) {
  border-radius: 6px;
  border-color: #d1d5db;
}

.pagination :deep(.ant-pagination-item-active) {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-color: transparent;
}

.pagination :deep(.ant-pagination-item-active a) {
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #spaceDetailPage {
    padding: 16px;
  }
  
  .space-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .space-info {
    min-width: auto;
    width: 100%;
  }
  
  .space-name {
    font-size: 1.5rem;
  }
  
  .space-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .space-stats {
    flex-direction: column;
    gap: 12px;
  }
  
  .space-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .action-btn {
    flex: 1;
    justify-content: center;
  }
  
  .storage-progress {
    margin-left: 0;
    margin-top: 16px;
  }
  
  .search-card,
  .pictures-card {
    padding: 16px;
  }
  
  .pagination-section {
    margin-bottom: 16px;
  }
  
  .pagination {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .space-name {
    font-size: 1.3rem;
  }
  
  .space-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .action-btn {
    width: 100%;
  }
}
</style>
