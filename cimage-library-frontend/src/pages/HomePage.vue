<template>
  <div id="homePage">
    <!-- Hero区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">发现精美图片</h1>
        <p class="hero-subtitle">探索海量高质量图片资源，为你的创作注入灵感</p>
        <!-- 现代化搜索框 -->
        <div class="modern-search-bar">
          <a-input-search
            v-model:value="searchParams.searchText"
            placeholder="从海量图片中搜索..."
            enter-button="搜索"
            size="large"
            class="hero-search"
            @search="doSearch"
          />
        </div>
      </div>
    </div>

    <div class="content-section">
      <!-- 分类卡片 -->
      <div class="category-section">
        <h3 class="section-title">浏览分类</h3>
        <div class="category-cards">
          <div 
            class="category-card" 
            :class="{ active: selectedCategory === 'all' }"
            @click="selectCategory('all')"
          >
            <div class="category-icon-wrapper">
              <svg class="category-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
            </div>
            <span class="category-name">全部</span>
          </div>
          <div 
            v-for="category in categoryList" 
            :key="category"
            class="category-card"
            :class="{ active: selectedCategory === category }"
            @click="selectCategory(category)"
          >
            <div class="category-icon-wrapper">
              <svg class="category-icon" viewBox="0 0 24 24" fill="currentColor">
                <path :d="getCategoryIconPath(category)"/>
              </svg>
            </div>
            <span class="category-name">{{ category }}</span>
          </div>
        </div>
      </div>

      <!-- 标签云 -->
      <div class="tag-section">
        <h3 class="section-title">热门标签</h3>
        <div class="tag-cloud">
          <a-checkable-tag
            v-for="(tag, index) in tagList"
            :key="tag"
            v-model:checked="selectedTagList[index]"
            class="cloud-tag"
            :class="{ 'tag-selected': selectedTagList[index] }"
            @change="() => onTagChange(index)"
          >
            {{ tag }}
          </a-checkable-tag>
        </div>
      </div>

      <!-- 图片列表 -->
      <PictureList :dataList="dataList" :loading="loading" />
      
      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination
          v-model:current="searchParams.current"
          v-model:pageSize="searchParams.pageSize"
          :total="total"
          :show-size-changer="false"
          :show-quick-jumper="true"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import PictureList from '@/components/PictureList.vue'

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 标签和分类列表
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 分类图标SVG路径映射
const getCategoryIconPath = (category: string) => {
  const iconPathMap: Record<string, string> = {
    '模板': 'M4 4h16v2H4V4zm0 4h16v2H4V8zm0 4h16v2H4v-2zm0 4h16v2H4v-2z',
    '电商': 'M7 4V2a1 1 0 0 1 1-1h8a1 1 0 0 1 1 1v2h4v2h-1l-1 10a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1L4 6H3V4h4zM9 3v1h6V3H9zm1 4v8h4V7h-4z',
    '表情包': 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
    '素材': 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z',
    '海报': 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
    '生活': 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
    '高清': 'M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z',
    '艺术': 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z',
    '极简': 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z',
    '背景': 'M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM5 17l3.5-4.5 2.5 3.01L14.5 11l4.5 6H5z',
    '简历': 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6zm4 18H6V4h7v5h5v11z',
    '创意': 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'
  }
  return iconPathMap[category] || 'M20 6h-2.18c.11-.31.18-.65.18-1a2.996 2.996 0 0 0-5.5-1.65l-.5.67-.5-.68C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-2 .89-2 2v11c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2z'
}

// 选择分类
const selectCategory = (category: string) => {
  selectedCategory.value = category
  doSearch()
}

// 标签变化处理
const onTagChange = (index: number) => {
  // 使用 v-model:checked 时，selectedTagList[index] 会自动更新
  doSearch()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  // [true, false, false] => ['java']
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags.push(tagList.value[index])
    }
  })
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
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

/**
 * 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
    // 初始化标签选择状态
    selectedTagList.value = new Array(tagList.value.length).fill(false)
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})
</script>

<style scoped>
#homePage {
  margin: 0;
  padding: 0;
}

/* Hero区域样式 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  padding: 80px 0 100px;
  text-align: center;
  color: white;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 40px;
  opacity: 0.9;
}

.modern-search-bar {
  max-width: 600px;
  margin: 0 auto;
}

.hero-search :deep(.ant-input) {
  border-radius: 50px;
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  padding: 12px 24px;
  font-size: 16px;
}

.hero-search :deep(.ant-btn) {
  border-radius: 50px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  font-weight: 600;
  padding: 0 32px;
}

.hero-search :deep(.ant-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
}

/* 内容区域 */
.content-section {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 24px;
  color: #2c3e50;
}

/* 分类卡片样式 */
.category-section {
  margin-bottom: 50px;
}

.category-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: flex-start;
}

.category-card {
  background: white;
  border: 2px solid #e1e8ed;
  border-radius: 16px;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  min-width: 120px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.category-card.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
}

.category-icon-wrapper {
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.category-icon {
  width: 32px;
  height: 32px;
  transition: all 0.3s ease;
}

.category-card:hover .category-icon {
  transform: scale(1.1);
}

.category-card.active .category-icon {
  color: white;
}

.category-name {
  font-weight: 500;
  font-size: 14px;
}

/* 标签云样式 */
.tag-section {
  margin-bottom: 50px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.cloud-tag {
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 2px solid #e1e8ed;
  background: white;
}

.cloud-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cloud-tag.tag-selected {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 分页样式 */
.pagination-container {
  text-align: center;
  margin-top: 40px;
  padding: 20px 0;
}

.pagination-container :deep(.ant-pagination-item) {
  border-radius: 8px;
}

.pagination-container :deep(.ant-pagination-item-active) {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .category-cards {
    justify-content: center;
  }
  
  .category-card {
    min-width: 100px;
    padding: 16px 20px;
  }
  
  .content-section {
    padding: 20px 16px;
  }
}
</style>
