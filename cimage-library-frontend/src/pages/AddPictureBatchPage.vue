<template>
  <!-- @author Kryos -->
  <div id="addPictureBatchPage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">📦 批量创建图片</h1>
        <p class="page-subtitle">通过关键词自动抓取并批量创建图片，提升工作效率</p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-container">
      <!-- 批量创建表单卡片 -->
      <div class="form-card">
        <div class="card-header">
          <h3 class="card-title">🎯 批量任务配置</h3>
          <p class="card-subtitle">设置抓取参数，系统将自动为您批量创建图片</p>
        </div>
        
        <div class="card-body">
          <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
            <!-- 关键词输入 -->
            <a-form-item name="searchText" label="关键词" class="form-item-enhanced">
              <div class="input-wrapper">
                <span class="input-icon">🔍</span>
                <a-input 
                  v-model:value="formData.searchText" 
                  placeholder="请输入搜索关键词，如：风景、建筑、人物等" 
                  allow-clear 
                  class="enhanced-input"
                />
              </div>
            </a-form-item>

            <!-- 抓取数量 -->
            <a-form-item name="count" label="抓取数量" class="form-item-enhanced">
              <div class="number-input-wrapper">
                <span class="input-icon">📊</span>
                <a-input-number
                  v-model:value="formData.count"
                  placeholder="请输入抓取数量"
                  class="enhanced-number-input"
                  :min="1"
                  :max="30"
                  allow-clear
                />
                <span class="input-hint">最多30张</span>
              </div>
            </a-form-item>

            <!-- 名称前缀 -->
            <a-form-item name="namePrefix" label="名称前缀" class="form-item-enhanced">
              <div class="input-wrapper">
                <span class="input-icon">🏷️</span>
                <a-input
                  v-model:value="formData.namePrefix"
                  placeholder="请输入名称前缀，系统会自动补充序号"
                  allow-clear
                  class="enhanced-input"
                />
              </div>
            </a-form-item>

            <!-- 提交按钮 -->
            <a-form-item class="submit-section">
              <a-button 
                type="primary" 
                html-type="submit" 
                :loading="loading"
                class="submit-button"
                size="large"
              >
                <template v-if="!loading">
                  <span class="button-icon">🚀</span>
                  <span>执行批量任务</span>
                </template>
                <template v-else>
                  <span>任务执行中...</span>
                </template>
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 使用说明卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h3 class="card-title">💡 使用说明</h3>
        </div>
        <div class="card-body">
          <div class="info-list">
            <div class="info-item">
              <span class="info-icon">🎯</span>
              <div class="info-content">
                <h4>关键词选择</h4>
                <p>输入清晰的关键词，如"自然风景"、"商务办公"等，有助于获得更精准的图片</p>
              </div>
            </div>
            <div class="info-item">
              <span class="info-icon">📊</span>
              <div class="info-content">
                <h4>数量控制</h4>
                <p>建议一次抓取10-20张图片，数量过多可能影响处理速度</p>
              </div>
            </div>
            <div class="info-item">
              <span class="info-icon">🏷️</span>
              <div class="info-content">
                <h4>命名规则</h4>
                <p>名称前缀将与序号组合，如前缀"风景"会生成"风景_001"、"风景_002"等</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
  uploadPictureByBatchUsingPost,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
// 提交任务状态
const loading = ref(false)

const router = useRouter()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  if (!formData.searchText?.trim()) {
    message.error('请输入搜索关键词')
    return
  }
  
  loading.value = true
  try {
    const res = await uploadPictureByBatchUsingPost({
      ...formData,
    })
    // 操作成功
    if (res.data.code === 0 && res.data.data) {
      message.success(`创建成功，共 ${res.data.data} 条`)
      // 跳转到主页
      router.push({
        path: `/`,
      })
    } else {
      message.error('创建失败，' + res.data.message)
    }
  } catch (error: any) {
    console.error('批量创建失败', error)
    // 处理不同类型的错误
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      message.error('请求超时，但任务可能仍在后台执行，请稍后查看结果')
    } else if (error.response?.data?.message) {
      message.error('创建失败，' + error.response.data.message)
    } else {
      message.error('创建失败，请检查网络连接后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#addPictureBatchPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.header-content {
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 30px 40px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin: 0 0 10px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
}

/* 内容容器 */
.content-container {
  max-width: 800px;
  margin: 0 auto;
  display: grid;
  gap: 20px;
}

/* 卡片基础样式 */
.form-card,
.info-card {
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
}

.form-card:hover,
.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 50px rgba(0, 0, 0, 0.15);
}

/* 卡片头部 */
.card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  padding: 25px 30px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 8px 0;
}

.card-subtitle {
  font-size: 0.95rem;
  color: #718096;
  margin: 0;
  line-height: 1.5;
}

/* 卡片内容 */
.card-body {
  padding: 30px;
}

/* 表单增强样式 */
.form-item-enhanced {
  margin-bottom: 24px;
}

.form-item-enhanced :deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #2d3748;
  font-size: 15px;
  margin-bottom: 8px;
}

/* 输入框包装器 */
.input-wrapper,
.number-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-icon {
  font-size: 16px;
  min-width: 20px;
  text-align: center;
}

/* 增强输入框 */
.enhanced-input,
.enhanced-number-input {
  flex: 1;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
}

.enhanced-input:hover,
.enhanced-number-input:hover {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.95);
}

.enhanced-input:focus,
.enhanced-number-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

/* 数字输入框特殊样式 */
.enhanced-number-input {
  min-width: 200px;
}

.input-hint {
  font-size: 12px;
  color: #a0aec0;
  font-weight: 500;
}

/* 提交按钮区域 */
.submit-section {
  margin-top: 30px;
  margin-bottom: 0;
}

.submit-button {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.submit-button:active {
  transform: translateY(0);
}

.button-icon {
  font-size: 16px;
}

/* 使用说明样式 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px;
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.info-item:hover {
  background: rgba(102, 126, 234, 0.08);
  border-color: rgba(102, 126, 234, 0.2);
  transform: translateX(5px);
}

.info-icon {
  font-size: 20px;
  min-width: 24px;
  text-align: center;
  margin-top: 2px;
}

.info-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 5px 0;
}

.info-content p {
  font-size: 13px;
  color: #718096;
  margin: 0;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #addPictureBatchPage {
    padding: 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .header-content {
    padding: 25px 20px;
  }
  
  .card-body {
    padding: 20px;
  }
  
  .number-input-wrapper {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .enhanced-number-input {
    min-width: unset;
    width: 100%;
  }
  
  .input-hint {
    text-align: center;
  }
}

/* 深色模式兼容 */
@media (prefers-color-scheme: dark) {
  .form-card,
  .info-card {
    background: rgba(26, 32, 44, 0.95);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  .card-title {
    color: #f7fafc;
  }
  
  .card-subtitle {
    color: #cbd5e0;
  }
  
  .enhanced-input,
  .enhanced-number-input {
    background: rgba(45, 55, 72, 0.8);
    border-color: #4a5568;
    color: #f7fafc;
  }
  
  .info-content h4 {
    color: #f7fafc;
  }
  
  .info-content p {
    color: #cbd5e0;
  }
}
</style>
