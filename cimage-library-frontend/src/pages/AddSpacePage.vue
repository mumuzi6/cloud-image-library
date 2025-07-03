<template>
  <div id="addSpacePage">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h2 class="page-title">
          {{ route.query?.id ? '修改' : '创建' }} {{ SPACE_TYPE_MAP[spaceType] }}
        </h2>
      </div>

      <!-- 表单区域 -->
      <div class="form-section">
        <div class="form-card">
          <h3 class="section-title">空间信息</h3>
          <a-form 
            name="spaceForm" 
            layout="vertical" 
            :model="spaceForm" 
            @finish="handleSubmit"
            class="modern-form"
          >
            <a-form-item name="spaceName" label="空间名称">
              <a-input 
                v-model:value="spaceForm.spaceName" 
                placeholder="请输入空间名称" 
                allow-clear 
                size="large"
                class="modern-input"
              />
            </a-form-item>
            <a-form-item name="spaceLevel" label="空间级别">
              <a-select
                v-model:value="spaceForm.spaceLevel"
                placeholder="请选择空间级别"
                :options="SPACE_LEVEL_OPTIONS"
                allow-clear
                size="large"
                class="modern-select"
              />
            </a-form-item>
            <a-form-item>
              <a-button 
                type="primary" 
                html-type="submit" 
                :loading="loading" 
                size="large"
                class="submit-button"
              >
                {{ route.query?.id ? '保存修改' : '创建空间' }}
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 空间级别介绍 -->
      <div class="info-section">
        <div class="info-card">
          <div class="card-header">
            <h3 class="section-title">空间级别介绍</h3>
          </div>
          <div class="card-content">
            <div class="notice-box">
              <div class="notice-icon">💡</div>
              <div class="notice-text">
                目前仅支持开通普通版，如需升级空间，请联系
                <a href="https://github.com/mumuzi6" target="_blank" class="contact-link">Kryos</a>
              </div>
            </div>
            
            <div class="level-list">
              <div 
                v-for="spaceLevel in spaceLevelList" 
                :key="spaceLevel.value"
                class="level-item"
              >
                <div class="level-name">{{ spaceLevel.text }}</div>
                <div class="level-specs">
                  <span class="spec-item">
                    <span class="spec-label">容量：</span>
                    <span class="spec-value">{{ formatSize(spaceLevel.maxSize) }}</span>
                  </span>
                  <span class="spec-item">
                    <span class="spec-label">数量：</span>
                    <span class="spec-value">{{ spaceLevel.maxCount }}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { useRoute, useRouter } from 'vue-router'
import {SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM, SPACE_TYPE_MAP} from '@/constants/space.ts'
import { formatSize } from '../utils'

const space = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest & { spaceLevel?: number }>({
  spaceLevel: undefined
})
const loading = ref(false)

const route = useRoute()
// 空间类别，默认为私有空间
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query.type)
  } else {
    return SPACE_TYPE_ENUM.PRIVATE
  }
})

const spaceLevelList = ref<API.SpaceLevel[]>([])

// 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('获取空间级别失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

const router = useRouter()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  if (spaceId) {
    // 更新
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
  } else {
    // 创建
    res = await addSpaceUsingPost({
      ...spaceForm,
      spaceType: spaceType.value,
    })
  }
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    // 跳转到空间详情页
    router.push({
      path: `/space/${res.data.data}`,
    })
  } else {
    message.error('操作失败，' + res.data.message)
  }
  loading.value = false
}

// 获取老数据
const getOldSpace = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id && typeof id === 'string') {
    const res = await getSpaceVoByIdUsingGet({
      id: Number(id),
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      // 填充表单
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    }
  }
}

onMounted(() => {
  getOldSpace()
})
</script>

<style scoped>
#addSpacePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.page-container {
  max-width: 800px;
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
  margin-bottom: 12px;
  text-shadow: none;
}

/* 表单区域 */
.form-section {
  margin-bottom: 32px;
}

.form-card,
.info-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.form-card:hover,
.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 24px;
  text-align: center;
}

/* 表单样式 */
.modern-form :deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.modern-input :deep(.ant-input) {
  border-radius: 12px;
  border: 2px solid #e1e8ed;
  padding: 12px 16px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: rgba(248, 250, 252, 0.8);
  height: 48px;
  line-height: 1.5;
}

.modern-input :deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.modern-select :deep(.ant-select-selector) {
  border-radius: 12px !important;
  border: 2px solid #e1e8ed !important;
  padding: 12px 16px !important;
  background: rgba(248, 250, 252, 0.8) !important;
  transition: all 0.3s ease;
  min-height: 48px !important;
  height: 48px !important;
  display: flex !important;
  align-items: center !important;
}

.modern-select :deep(.ant-select-selection-search-input) {
  height: 100% !important;
  line-height: 1.5 !important;
}

.modern-select :deep(.ant-select-selection-placeholder) {
  line-height: 1.5 !important;
  display: flex !important;
  align-items: center !important;
  height: 100% !important;
}

.modern-select :deep(.ant-select-focused .ant-select-selector) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
  background: white !important;
}

.modern-select :deep(.ant-select-selection-item) {
  line-height: 1.5 !important;
  display: flex !important;
  align-items: center !important;
  height: 100% !important;
}

.modern-select :deep(.ant-select-arrow) {
  top: 50% !important;
  transform: translateY(-50%) !important;
}

.submit-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  margin-top: 16px;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
}

.submit-button:active {
  transform: translateY(0);
}

/* 信息区域 */
.info-section {
  margin-bottom: 32px;
}

.card-header {
  margin-bottom: 24px;
}

.card-content {
  space-y: 24px;
}

.notice-box {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  margin-bottom: 24px;
}

.notice-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.notice-text {
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
}

.contact-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.contact-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.level-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.level-item {
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e1e8ed;
  transition: all 0.3s ease;
}

.level-item:hover {
  background: white;
  border-color: #667eea;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.level-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.level-specs {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.spec-label {
  color: #6b7280;
  font-weight: 500;
}

.spec-value {
  color: #374151;
  font-weight: 600;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #addSpacePage {
    padding: 16px;
  }
  
  .page-container {
    max-width: 100%;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .form-card,
  .info-card {
    padding: 24px 20px;
  }
  
  .level-specs {
    flex-direction: column;
    gap: 8px;
  }
  
  .notice-box {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
