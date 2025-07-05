<template>
  <div id="spaceAnalyzePage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            📊 空间图库分析
          </h1>
          <div class="page-subtitle">
            <span v-if="queryAll" class="subtitle-tag all">📈 全部空间</span>
            <span v-else-if="queryPublic" class="subtitle-tag public">🌍 公共图库</span>
            <span v-else class="subtitle-tag private">
              {{ getSpaceTypeIcon() }} <a :href="`/space/${spaceId}`" target="_blank" class="space-link">空间 ID：{{ spaceId }}</a>
            </span>
          </div>
          <p class="page-description">深入了解空间使用情况，优化存储策略</p>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-icon">📊</div>
            <div class="stat-info">
              <div class="stat-label">分析类型</div>
              <div class="stat-value">
                {{ getAnalysisType() }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分析网格 -->
    <div class="analysis-grid">
      <a-row :gutter="[24, 24]">
        <!-- 空间使用分析 -->
        <a-col :xs="24" :lg="12">
          <div class="analysis-card">
            <div class="card-header">
              <h3 class="card-title">💾 存储空间</h3>
              <p class="card-description">空间使用情况统计</p>
            </div>
            <div class="card-content">
              <SpaceUsageAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
            </div>
          </div>
        </a-col>
        
        <!-- 空间分类分析 -->
        <a-col :xs="24" :lg="12">
          <div class="analysis-card">
            <div class="card-header">
              <h3 class="card-title">🏷️ 图片分类</h3>
              <p class="card-description">按分类统计图片分布</p>
            </div>
            <div class="card-content">
              <SpaceCategoryAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
            </div>
          </div>
        </a-col>
        
        <!-- 标签分析 -->
        <a-col :xs="24" :lg="12">
          <div class="analysis-card">
            <div class="card-header">
              <h3 class="card-title">🏷️ 标签分析</h3>
              <p class="card-description">热门标签使用统计</p>
            </div>
            <div class="card-content">
              <SpaceTagAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
            </div>
          </div>
        </a-col>
        
        <!-- 图片大小分段分析 -->
        <a-col :xs="24" :lg="12">
          <div class="analysis-card">
            <div class="card-header">
              <h3 class="card-title">📏 图片尺寸</h3>
              <p class="card-description">按文件大小分段统计</p>
            </div>
            <div class="card-content">
              <SpaceSizeAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
            </div>
          </div>
        </a-col>
        
        <!-- 用户上传行为分析 -->
        <a-col :xs="24" :lg="12">
          <div class="analysis-card">
            <div class="card-header">
              <h3 class="card-title">👥 用户行为</h3>
              <p class="card-description">用户上传活跃度分析</p>
            </div>
            <div class="card-content">
              <SpaceUserAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
            </div>
          </div>
        </a-col>
        
        <!-- 空间使用排行分析 -->
        <a-col :xs="24" :lg="12" v-if="isAdmin">
          <div class="analysis-card">
            <div class="card-header">
              <h3 class="card-title">🏆 使用排行</h3>
              <p class="card-description">空间使用排行榜</p>
            </div>
            <div class="card-content">
              <SpaceRankAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
            </div>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import SpaceUsageAnalyze from '@/components/analyze/SpaceUsageAnalyze.vue'
import SpaceCategoryAnalyze from '@/components/analyze/SpaceCategoryAnalyze.vue'
import SpaceTagAnalyze from '@/components/analyze/SpaceTagAnalyze.vue'
import SpaceSizeAnalyze from '@/components/analyze/SpaceSizeAnalyze.vue'
import SpaceUserAnalyze from '@/components/analyze/SpaceUserAnalyze.vue'
import SpaceRankAnalyze from '@/components/analyze/SpaceRankAnalyze.vue'
import { useRoute } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { getSpaceByIdUsingGet } from '@/api/spaceController.ts'

const route = useRoute()

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId as string
})

// 是否查询所有空间
const queryAll = computed(() => {
  return !!route.query?.queryAll
})

// 是否查询公共空间
const queryPublic = computed(() => {
  return !!route.query?.queryPublic
})

// 判断用户是否为管理员
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
const isAdmin = computed(() => {
  return loginUser.userRole === 'admin'
})

// 空间信息
const spaceInfo = ref<API.Space | null>(null)

// 获取空间信息
const fetchSpaceInfo = async () => {
  if (!spaceId.value) return
  
  try {
    const res = await getSpaceByIdUsingGet({ id: spaceId.value })
    if (res.data.code === 0 && res.data.data) {
      spaceInfo.value = res.data.data
    }
  } catch (error) {
    console.error('获取空间信息失败:', error)
  }
}

// 获取分析类型
const getAnalysisType = () => {
  if (queryAll.value) return '全部空间'
  if (queryPublic.value) return '公共图库'
  
  if (spaceInfo.value) {
    return spaceInfo.value.spaceType === 0 ? '私有空间' : '团队空间'
  }
  
  return '私有空间' // 默认值
}

// 获取空间类型图标
const getSpaceTypeIcon = () => {
  if (spaceInfo.value) {
    return spaceInfo.value.spaceType === 0 ? '🔒' : '👥'
  }
  return '🏠' // 默认图标
}

// 页面加载时获取空间信息
onMounted(() => {
  if (spaceId.value) {
    fetchSpaceInfo()
  }
})
</script>

<style scoped>
#spaceAnalyzePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 24px;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
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

.header-info {
  flex: 1;
  min-width: 300px;
}

.page-title {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 16px 0;
}

.page-subtitle {
  margin-bottom: 12px;
}

.subtitle-tag {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.subtitle-tag.all {
  background: linear-gradient(45deg, #667eea, #764ba2);
}

.subtitle-tag.public {
  background: linear-gradient(45deg, #10b981, #059669);
}

.subtitle-tag.private {
  background: linear-gradient(45deg, #f59e0b, #d97706);
}

.space-link {
  color: white;
  text-decoration: none;
  font-weight: 600;
}

.space-link:hover {
  text-decoration: underline;
}

.page-description {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
  line-height: 1.5;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.stat-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 12px;
  color: white;
}

.stat-info {
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

/* 分析网格 */
.analysis-grid {
  margin-bottom: 32px;
}

.analysis-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: 100%;
  transition: all 0.3s ease;
}

.analysis-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.card-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-description {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.card-content {
  position: relative;
  min-height: 300px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .analysis-grid :deep(.ant-col-lg-12) {
    width: 100%;
  }
}

@media (max-width: 768px) {
  #spaceAnalyzePage {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-info {
    min-width: auto;
    width: 100%;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .header-stats {
    width: 100%;
  }
  
  .stat-item {
    flex: 1;
    padding: 12px 16px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .analysis-card {
    padding: 16px;
  }
  
  .card-content {
    min-height: 250px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .header-stats {
    flex-direction: column;
  }
  
  .stat-item {
    flex: none;
  }
  
  .analysis-card {
    padding: 12px;
  }
  
  .card-content {
    min-height: 200px;
  }
}
</style>
