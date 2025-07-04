<template>
  <div id="pictureDetailPage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">📷 图片详情</h1>
        <p class="page-subtitle">查看图片的详细信息和元数据</p>
      </div>
    </div>

    <a-row :gutter="[24, 24]">
      <!-- 图片预览 -->
      <a-col :sm="24" :md="16" :xl="18">
        <div class="preview-card glass-card">
          <div class="card-header">
            <h3 class="card-title">🖼️ 图片预览</h3>
          </div>
          <div class="image-container">
            <a-image 
              :src="picture.url" 
              class="preview-image"
              :style="{ 
                maxHeight: '600px', 
                objectFit: 'contain',
                borderRadius: '12px',
                boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)'
              }" 
            />
          </div>
        </div>
      </a-col>
      
      <!-- 图片信息区域 -->
      <a-col :sm="24" :md="8" :xl="6">
        <div class="info-card glass-card">
          <div class="card-header">
            <h3 class="card-title">📋 图片信息</h3>
          </div>
          
          <!-- 作者信息 -->
          <div class="author-section">
            <div class="author-info">
              <a-avatar :size="36" :src="picture.user?.userAvatar" class="author-avatar" />
              <div class="author-details">
                <div class="author-name">{{ picture.user?.userName }}</div>
                <div class="author-label">作者</div>
              </div>
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="info-section">
            <div class="info-item">
              <span class="info-label">📝 名称</span>
              <span class="info-value">{{ picture.name ?? '未命名' }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">📄 简介</span>
              <span class="info-value">{{ picture.introduction ?? '-' }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">📂 分类</span>
              <span class="info-value category-tag">{{ picture.category ?? '默认' }}</span>
            </div>
            
            <div class="info-item">
              <span class="info-label">🏷️ 标签</span>
              <div class="tags-container">
                <a-tag 
                  v-for="tag in picture.tags" 
                  :key="tag"
                  class="picture-tag"
                  color="blue"
                >
                  {{ tag }}
                </a-tag>
                <span v-if="!picture.tags || picture.tags.length === 0" class="info-value">-</span>
              </div>
            </div>
          </div>

          <!-- 技术规格 -->
          <div class="tech-section">
            <h4 class="section-title">⚙️ 技术规格</h4>
            
            <div class="spec-grid">
              <div class="spec-item">
                <span class="spec-label">格式</span>
                <span class="spec-value format-badge">{{ picture.picFormat ?? '-' }}</span>
              </div>
              
              <div class="spec-item">
                <span class="spec-label">尺寸</span>
                <span class="spec-value">{{ picture.picWidth ?? '-' }} × {{ picture.picHeight ?? '-' }}</span>
              </div>
              
              <div class="spec-item">
                <span class="spec-label">宽高比</span>
                <span class="spec-value">{{ picture.picScale ?? '-' }}</span>
              </div>
              
              <div class="spec-item">
                <span class="spec-label">大小</span>
                <span class="spec-value size-badge">{{ formatSize(picture.picSize) }}</span>
              </div>
              
              <div class="spec-item" v-if="picture.picColor">
                <span class="spec-label">主色调</span>
                <div class="color-info">
                  <span class="color-code">{{ picture.picColor }}</span>
                  <div
                    class="color-preview"
                    :style="{
                      backgroundColor: toHexColor(picture.picColor),
                    }"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="actions-section">
            <div class="primary-actions">
              <a-button 
                type="primary" 
                size="large"
                @click="doDownload"
                class="download-btn"
              >
                <template #icon>
                  <DownloadOutlined />
                </template>
                免费下载
              </a-button>
              
              <a-button 
                :icon="h(ShareAltOutlined)" 
                type="primary" 
                ghost 
                size="large"
                @click="doShare"
                class="share-btn"
              >
                分享
              </a-button>
            </div>
            
            <div class="secondary-actions" v-if="canEdit || canDelete">
              <a-button 
                v-if="canEdit" 
                :icon="h(EditOutlined)" 
                type="default" 
                @click="doEdit"
                class="edit-btn"
              >
                编辑
              </a-button>
              
              <a-button 
                v-if="canDelete" 
                :icon="h(DeleteOutlined)" 
                danger 
                @click="doDelete"
                class="delete-btn"
              >
                删除
              </a-button>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
    
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { downloadImage, formatSize, toHexColor } from '@/utils'
import ShareModal from '@/components/ShareModal.vue'
import { SPACE_PERMISSION_ENUM } from '@/constants/space.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (picture.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canEdit = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDelete = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchPictureDetail()
})

const router = useRouter()

// 编辑
const doEdit = () => {
  router.push({
    path: '/add_picture',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId,
    },
  })
}

// 删除数据
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id: Number(id) })
  if (res.data.code === 0) {
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}

// 下载图片
const doDownload = () => {
  downloadImage(picture.value.url ?? '')
}

// ----- 分享操作 ----
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()
// 分享
const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
#pictureDetailPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  padding: 20px;
  position: relative;
}

/* 添加纹理效果 */
#pictureDetailPage::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.header-content {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 32px 40px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
}

/* 玻璃卡片效果 */
.glass-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.glass-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 卡片头部 */
.card-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.card-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: white;
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 图片预览区域 */
.preview-card {
  padding: 0;
  overflow: hidden;
}

.image-container {
  padding: 24px;
  text-align: center;
  background: rgba(255, 255, 255, 0.05);
}

.preview-image {
  max-width: 100%;
  height: auto;
  transition: transform 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.02);
}

/* 信息卡片 */
.info-card {
  padding: 0;
}

/* 作者信息 */
.author-section {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.author-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

/* 基本信息 */
.info-section {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.info-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: white;
  font-weight: 500;
  text-align: right;
  word-break: break-all;
}

.category-tag {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.picture-tag {
  background: linear-gradient(45deg, #1890ff, #40a9ff) !important;
  border: none !important;
  color: white !important;
  border-radius: 8px !important;
  font-weight: 500 !important;
}

/* 技术规格 */
.tech-section {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.section-title {
  font-size: 1rem;
  color: white;
  margin: 0 0 16px 0;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.spec-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.spec-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.spec-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  min-width: 60px;
  flex-shrink: 0;
}

.spec-value {
  color: white;
  font-weight: 500;
}

.format-badge {
  background: linear-gradient(45deg, #52c41a, #73d13d);
  color: white;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 0.85rem;
  text-transform: uppercase;
}

.size-badge {
  background: linear-gradient(45deg, #fa8c16, #ffa940);
  color: white;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 0.85rem;
}

.color-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-code {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 0.85rem;
}

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* 操作按钮区域 */
.actions-section {
  padding: 24px;
}

.primary-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.primary-actions .ant-btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  font-weight: 600;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.download-btn {
  background: linear-gradient(45deg, #52c41a, #73d13d) !important;
}

.download-btn:hover {
  background: linear-gradient(45deg, #389e0d, #52c41a) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2) !important;
}

.share-btn {
  background: rgba(255, 255, 255, 0.15) !important;
  backdrop-filter: blur(10px) !important;
  color: white !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
}

.share-btn:hover {
  background: rgba(255, 255, 255, 0.25) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2) !important;
}

.secondary-actions {
  display: flex;
  gap: 12px;
}

.secondary-actions .ant-btn {
  flex: 1;
  height: 40px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.edit-btn {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: translateY(-1px);
}

.delete-btn:hover {
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  #pictureDetailPage {
    padding: 16px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .header-content {
    padding: 24px 20px;
  }
  
  .primary-actions {
    flex-direction: column;
  }
  
  .secondary-actions {
    flex-direction: column;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  
  .info-value {
    text-align: left;
  }
  
  .tags-container {
    justify-content: flex-start;
  }
  
  .spec-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.75rem;
  }
  
  .header-content {
    padding: 20px 16px;
  }
  
  .card-header {
    padding: 16px 20px 12px;
  }
  
  .author-section,
  .info-section,
  .tech-section {
    padding: 16px 20px;
  }
  
  .actions-section {
    padding: 20px;
  }
}
</style>
