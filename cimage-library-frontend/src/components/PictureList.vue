<template>
  <div class="picture-list">
    <!-- 图片列表 - 采用现代瀑布流设计 -->
    <div class="masonry-grid" v-if="dataList.length > 0">
      <div 
        v-for="picture in dataList" 
        :key="picture.id"
        class="masonry-item"
        @click="doClickPicture(picture)"
      >
        <div class="picture-card">
          <!-- 图片容器 -->
          <div class="image-container">
            <img
              :alt="picture.name || '图片'"
              :src="picture.thumbnailUrl ?? picture.url"
              class="picture-image"
              loading="lazy"
            />
            <!-- 基础操作按钮 - 始终显示 -->
            <div v-if="showOp" class="basic-actions">
              <div class="basic-action-btn" @click="(e) => doShare(picture, e)" title="分享">
                <ShareAltOutlined />
              </div>
              <div class="basic-action-btn" @click="(e) => doSearch(picture, e)" title="以图搜图">
                <SearchOutlined />
              </div>
            </div>
            
            <!-- 高级操作按钮 - 悬浮显示 -->
            <div v-if="showOp && (canEdit || canDelete)" class="action-overlay">
              <div class="action-buttons">
                <div v-if="canEdit" class="action-btn edit-btn" @click="(e) => doEdit(picture, e)" title="编辑">
                  <EditOutlined />
                </div>
                <div v-if="canDelete" class="action-btn delete-btn" @click="(e) => doDelete(picture, e)" title="删除">
                  <DeleteOutlined />
                </div>
              </div>
            </div>
          </div>
          
          <!-- 图片信息 -->
          <div class="picture-info">
            <h4 class="picture-title">{{ picture.name }}</h4>
            <div class="picture-tags">
              <span class="category-tag">
                {{ picture.category ?? '默认' }}
              </span>
              <span 
                v-for="tag in (picture.tags || []).slice(0, 3)" 
                :key="tag"
                class="tag-item"
              >
                {{ tag }}
              </span>
              <span v-if="(picture.tags || []).length > 3" class="more-tags">
                +{{ (picture.tags || []).length - 3 }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-grid">
        <div v-for="n in 12" :key="n" class="loading-card">
          <div class="loading-image"></div>
          <div class="loading-content">
            <div class="loading-title"></div>
            <div class="loading-tags">
              <div class="loading-tag"></div>
              <div class="loading-tag"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && dataList.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M32 8C18.745 8 8 18.745 8 32s10.745 24 24 24 24-10.745 24-24S45.255 8 32 8zm0 44c-11.046 0-20-8.954-20-20s8.954-20 20-20 20 8.954 20 20-8.954 20-20 20z" fill="#E1E8ED"/>
          <path d="M44 28H20v16h24V28zm-4 12H24V32h16v8z" fill="#E1E8ED"/>
        </svg>
      </div>
      <h3 class="empty-title">暂无图片</h3>
      <p class="empty-description">换个关键词试试吧</p>
    </div>

    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 搜索
const doSearch = (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  if (!picture.id) {
    message.error('图片ID不存在')
    return
  }
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  // 检查图片ID是否存在
  if (!picture.id) {
    message.error('图片ID不存在')
    return
  }
  // 跳转到专门的编辑页面
  router.push({
    path: `/edit_picture/${String(picture.id)}`,
  })
}

// 删除数据
const doDelete = async (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}

// ----- 分享操作 ----
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()
// 分享
const doShare = (picture: API.PictureVO, e: Event) => {
  // 阻止冒泡
  e.stopPropagation()
  if (!picture.id) {
    message.error('图片ID不存在')
    return
  }
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
.picture-list {
  width: 100%;
}

/* 瀑布流网格布局 */
.masonry-grid {
  columns: 6;
  column-gap: 16px;
  padding: 0;
}

/* 响应式列数调整 */
@media (max-width: 1600px) {
  .masonry-grid {
    columns: 5;
  }
}

@media (max-width: 1280px) {
  .masonry-grid {
    columns: 4;
  }
}

@media (max-width: 1024px) {
  .masonry-grid {
    columns: 3;
  }
}

@media (max-width: 768px) {
  .masonry-grid {
    columns: 2;
  }
}

@media (max-width: 480px) {
  .masonry-grid {
    columns: 1;
  }
}

/* 瀑布流项目 */
.masonry-item {
  break-inside: avoid;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 图片卡片 */
.picture-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(229, 232, 235, 0.8);
}

.picture-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border-color: rgba(102, 126, 234, 0.3);
}

/* 图片容器 */
.image-container {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.picture-image {
  width: 100%;
  height: auto;
  display: block;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  object-fit: cover;
}

.picture-card:hover .picture-image {
  transform: scale(1.05);
}

/* 悬浮操作层 */
.action-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9), rgba(118, 75, 162, 0.9));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);
}

.picture-card:hover .action-overlay {
  opacity: 1;
}

.action-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateY(8px);
  opacity: 0;
}

.picture-card:hover .action-btn {
  transform: translateY(0);
  opacity: 1;
}

.action-btn:nth-child(1) { transition-delay: 0.1s; }
.action-btn:nth-child(2) { transition-delay: 0.15s; }
.action-btn:nth-child(3) { transition-delay: 0.2s; }
.action-btn:nth-child(4) { transition-delay: 0.25s; }

.action-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px) scale(1.1);
  border-color: rgba(255, 255, 255, 0.4);
}

.action-btn.edit-btn:hover {
  background: rgba(52, 199, 89, 0.25);
  border-color: rgba(52, 199, 89, 0.4);
}

.action-btn.delete-btn:hover {
  background: rgba(255, 59, 48, 0.25);
  border-color: rgba(255, 59, 48, 0.4);
}

/* 基础操作按钮样式 */
.basic-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.picture-card:hover .basic-actions {
  opacity: 1;
}

.basic-action-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a5568;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  transform: scale(0.9);
}

.picture-card:hover .basic-action-btn {
  transform: scale(1);
}

.basic-action-btn:hover {
  background: rgba(102, 126, 234, 0.9);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

/* 图片信息区域 */
.picture-info {
  padding: 16px 18px 18px;
}

.picture-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.picture-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.category-tag {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.tag-item {
  background: #f7fafc;
  color: #4a5568;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  border: 1px solid #e2e8f0;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.more-tags {
  color: #718096;
  font-size: 12px;
  font-weight: 500;
}

/* 加载状态 */
.loading-container {
  width: 100%;
}

.loading-grid {
  columns: 6;
  column-gap: 16px;
}

@media (max-width: 1600px) {
  .loading-grid {
    columns: 5;
  }
}

@media (max-width: 1280px) {
  .loading-grid {
    columns: 4;
  }
}

@media (max-width: 1024px) {
  .loading-grid {
    columns: 3;
  }
}

@media (max-width: 768px) {
  .loading-grid {
    columns: 2;
  }
}

@media (max-width: 480px) {
  .loading-grid {
    columns: 1;
  }
}

.loading-card {
  break-inside: avoid;
  margin-bottom: 16px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e5e8eb;
}

.loading-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

.loading-content {
  padding: 16px 18px 18px;
}

.loading-title {
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.loading-tags {
  display: flex;
  gap: 6px;
}

.loading-tag {
  height: 20px;
  width: 60px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 8px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #718096;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #4a5568;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  color: #a0aec0;
  margin: 0;
}

/* 入场动画 */
.masonry-item {
  animation: fadeInUp 0.5s ease-out;
}

.masonry-item:nth-child(1) { animation-delay: 0.1s; }
.masonry-item:nth-child(2) { animation-delay: 0.2s; }
.masonry-item:nth-child(3) { animation-delay: 0.3s; }
.masonry-item:nth-child(4) { animation-delay: 0.4s; }
.masonry-item:nth-child(5) { animation-delay: 0.5s; }
.masonry-item:nth-child(6) { animation-delay: 0.6s; }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
