<template>
  <div id="searchPicturePage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">🔍</div>
        <div class="header-text">
          <h1>以图搜图</h1>
          <p>通过图片相似度匹配，找到相关图片资源</p>
        </div>
      </div>
    </div>

    <!-- 原图展示区域 -->
    <div class="source-section">
      <div class="section-header">
        <h2>📸 原图</h2>
        <p>基于此图片进行相似度搜索</p>
      </div>
      <div class="source-image-container">
        <a-card hoverable class="source-image-card">
          <template #cover>
            <img
              :alt="picture.name"
              :src="picture.thumbnailUrl ?? picture.url"
              class="source-image"
            />
          </template>
          <div class="source-image-info">
            <h3>{{ picture.name }}</h3>
            <p v-if="picture.tags">标签：{{ picture.tags }}</p>
          </div>
        </a-card>
      </div>
    </div>

    <!-- 搜索结果区域 -->
    <div class="results-section">
      <div class="section-header">
        <h2>🎯 识图结果</h2>
        <p>找到 {{ dataList.length }} 张相似图片</p>
      </div>
      
      <!-- 图片结果列表 -->
      <div class="results-container">
        <a-list
          :grid="{ gutter: 20, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
          :data-source="dataList"
          :loading="loading"
        >
          <template #renderItem="{ item: picture }">
            <a-list-item style="padding: 0">
              <a :href="picture.fromUrl" target="_blank" class="result-link">
                <!-- 单张图片 -->
                <a-card hoverable class="result-card">
                  <template #cover>
                    <div class="result-image-container">
                      <img
                        :alt="picture.name"
                        :src="picture.thumbUrl"
                        class="result-image"
                      />
                      <div class="result-overlay">
                        <div class="overlay-content">
                          <span class="view-icon">👁️</span>
                          <span>查看详情</span>
                        </div>
                      </div>
                    </div>
                  </template>
                </a-card>
              </a>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  getPictureVoByIdUsingGet,
  searchPictureByPictureUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const pictureId = computed(() => {
  return route.query?.pictureId as string | undefined
})
const picture = ref<API.PictureVO>({})

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
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

// 以图搜图结果
const dataList = ref<API.ImageSearchResult[]>([])
const loading = ref<boolean>(true)
// 获取搜图结果
const fetchResultData = async () => {
  loading.value = true;
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value as string,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取数据失败，' + e.message)
  }
  loading.value = false;
}

// 页面加载时请求一次
onMounted(() => {
  fetchResultData()
})
</script>

<style scoped>
#searchPicturePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
  margin: 0;
}

/* 页面头部样式 */
.page-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 2rem 0;
  margin-bottom: 2rem;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.header-icon {
  font-size: 3rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 1rem;
  backdrop-filter: blur(10px);
}

.header-text h1 {
  color: white;
  margin: 0;
  font-size: 2.5rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.header-text p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0.5rem 0 0 0;
  font-size: 1.1rem;
}

/* 区域样式 */
.source-section,
.results-section {
  max-width: 1200px;
  margin: 0 auto 3rem auto;
  padding: 0 2rem;
}

.section-header {
  margin-bottom: 2rem;
  text-align: center;
}

.section-header h2 {
  color: white;
  font-size: 2rem;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.section-header p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0.5rem 0 0 0;
  font-size: 1rem;
}

/* 原图展示样式 */
.source-image-container {
  display: flex;
  justify-content: center;
}

.source-image-card {
  width: 320px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.source-image-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
}

.source-image {
  height: 240px;
  width: 100%;
  object-fit: cover;
  border-radius: 16px 16px 0 0;
}

.source-image-info {
  padding: 1rem;
}

.source-image-info h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.2rem;
  font-weight: 600;
}

.source-image-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

/* 搜索结果样式 */
.results-container {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.result-link {
  text-decoration: none;
  display: block;
}

.result-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
}

.result-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.result-image-container {
  position: relative;
  overflow: hidden;
}

.result-image {
  height: 200px;
  width: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.result-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.result-card:hover .result-overlay {
  opacity: 1;
}

.result-card:hover .result-image {
  transform: scale(1.1);
}

.overlay-content {
  text-align: center;
  color: white;
  font-size: 1rem;
  font-weight: 600;
}

.view-icon {
  display: block;
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 1rem;
    flex-direction: column;
    text-align: center;
  }
  
  .header-icon {
    font-size: 2rem;
    padding: 0.8rem;
  }
  
  .header-text h1 {
    font-size: 2rem;
  }
  
  .source-section,
  .results-section {
    padding: 0 1rem;
  }
  
  .source-image-card {
    width: 100%;
    max-width: 320px;
  }
  
  .results-container {
    padding: 1rem;
  }
  
  .section-header h2 {
    font-size: 1.5rem;
  }
}

/* 加载状态样式 */
:deep(.ant-spin-container) {
  background: transparent;
}

:deep(.ant-spin) {
  color: white;
}

:deep(.ant-spin-dot-item) {
  background-color: white;
}

/* 列表样式优化 */
:deep(.ant-list-item) {
  border: none;
  padding: 0 0 20px 0;
}

:deep(.ant-card-body) {
  padding: 0;
}
</style>
