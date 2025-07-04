<template>
  <div id="editPicturePage">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h2 class="page-title">修改图片</h2>
        <a-typography-paragraph v-if="spaceId" type="secondary" class="space-info">
                     所属空间：<a :href="`/space/${spaceId}`" target="_blank" class="space-link">{{ space?.spaceName || spaceId }}</a>
        </a-typography-paragraph>
      </div>

      <!-- 图片预览区域 -->
      <div v-if="picture" class="preview-section">
        <div class="preview-card">
          <h3 class="section-title">当前图片</h3>
          <div class="image-preview">
            <img 
              :src="picture.url" 
              :alt="picture.name || '图片预览'"
              class="preview-image"
            />
          </div>
        </div>
      </div>

      <!-- 图片编辑工具栏 -->
      <div v-if="picture" class="edit-section">
        <div class="edit-card">
          <h3 class="section-title">图片编辑工具</h3>
          <a-space size="middle" class="edit-actions">
            <a-button 
              :icon="h(EditOutlined)" 
              @click="doEditPicture"
              class="edit-button"
              size="large"
            >
              裁剪图片
            </a-button>
            <a-button 
              type="primary" 
              :icon="h(FullscreenOutlined)" 
              @click="doImagePainting"
              class="ai-button"
              size="large"
            >
              AI 扩图
            </a-button>
          </a-space>
        </div>
        
        <ImageCropper
          ref="imageCropperRef"
          :imageUrl="picture?.url"
          :picture="picture"
          :spaceId="spaceId"
          :space="space"
          :onSuccess="onCropSuccess"
        />
        <ImageOutPainting
          ref="imageOutPaintingRef"
          :picture="picture"
          :spaceId="spaceId"
          :onSuccess="onImageOutPaintingSuccess"
        />
      </div>

      <!-- 图片信息表单 -->
      <div v-if="picture" class="form-section">
        <div class="form-card">
          <h3 class="section-title">图片信息</h3>
          <a-form
            name="pictureForm"
            layout="vertical"
            :model="pictureForm"
            @finish="handleSubmit"
            class="modern-form"
          >
            <a-form-item name="name" label="名称">
              <a-input 
                v-model:value="pictureForm.name" 
                placeholder="请输入名称" 
                allow-clear 
                size="large"
                class="modern-input"
              />
            </a-form-item>
            <a-form-item name="introduction" label="简介">
              <a-textarea
                v-model:value="pictureForm.introduction"
                placeholder="请输入简介"
                :auto-size="{ minRows: 3, maxRows: 6 }"
                allow-clear
                class="modern-textarea"
              />
            </a-form-item>
            <a-form-item name="category" label="分类">
              <a-auto-complete
                v-model:value="pictureForm.category"
                placeholder="请输入分类"
                :options="categoryOptions"
                allow-clear
                size="large"
                class="modern-input"
              />
            </a-form-item>
            <a-form-item name="tags" label="标签">
              <a-select
                v-model:value="pictureForm.tags"
                mode="tags"
                placeholder="请输入标签"
                :options="tagOptions"
                allow-clear
                size="large"
                class="modern-select"
              />
            </a-form-item>
            <a-form-item>
              <a-space size="middle" class="form-actions">
                <a-button 
                  type="primary" 
                  html-type="submit" 
                  size="large"
                  class="submit-button"
                  :loading="submitting"
                >
                  保存修改
                </a-button>
                <a-button 
                  size="large"
                  @click="goBack"
                  class="cancel-button"
                >
                  取消
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <a-spin size="large" tip="加载图片信息中...">
          <div class="loading-placeholder"></div>
        </a-spin>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-section">
        <a-result
          status="error"
          title="加载失败"
          :sub-title="error"
        >
          <template #extra>
            <a-button type="primary" @click="loadPictureData">
              重新加载
            </a-button>
          </template>
        </a-result>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'

const router = useRouter()
const route = useRoute()

// 响应式数据
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const loading = ref(false)
const submitting = ref(false)
const error = ref('')

// 空间相关
const spaceId = computed(() => {
  return picture.value?.spaceId
})
const space = ref<API.SpaceVO>()

// 标签和分类选项
const categoryOptions = ref<{value: string, label: string}[]>([])
const tagOptions = ref<{value: string, label: string}[]>([])

/**
 * 获取图片ID
 */
const getPictureId = () => {
  const id = route.params.id
  return typeof id === 'string' ? id : undefined
}

/**
 * 加载图片数据
 */
const loadPictureData = async () => {
  const pictureId = getPictureId()
  if (!pictureId) {
    error.value = '图片ID不存在'
    return
  }

  loading.value = true
  error.value = ''
  
  try {
    // 使用字符串形式的ID来避免JavaScript大整数精度问题
    const res = await getPictureVoByIdUsingGet({
      id: pictureId,
    } as any)
    
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      // 填充表单数据
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
      
      // 加载空间信息
      if (data.spaceId) {
        await loadSpaceData(data.spaceId)
      }
    } else {
      error.value = res.data.message || '获取图片信息失败'
    }
  } catch (err: any) {
    error.value = err.message || '网络请求失败'
  } finally {
    loading.value = false
  }
}

/**
 * 加载空间数据
 */
const loadSpaceData = async (id: number) => {
  try {
    const res = await getSpaceVoByIdUsingGet({ id })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  } catch (err) {
    console.error('加载空间信息失败:', err)
  }
}

/**
 * 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => ({
        value: data,
        label: data,
      }))
      categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => ({
        value: data,
        label: data,
      }))
    }
  } catch (err) {
    console.error('获取标签分类列表失败:', err)
  }
}

/**
 * 提交表单
 */
const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    message.error('图片ID不存在')
    return
  }

  submitting.value = true
  
  try {
    // 使用字符串形式的ID来避免JavaScript大整数精度问题
    const res = await editPictureUsingPost({
      id: pictureId,
      spaceId: spaceId.value,
      ...values,
    } as any)
    
    if (res.data.code === 0) {
      message.success('修改成功')
      // 跳转到图片详情页
      router.push({
        path: `/picture/${pictureId}`,
      })
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (err: any) {
    message.error('修改失败：' + (err.message || '网络请求失败'))
  } finally {
    submitting.value = false
  }
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

// ----- 图片编辑器引用 ------
const imageCropperRef = ref()

/**
 * 编辑图片
 */
const doEditPicture = () => {
  imageCropperRef.value?.openModal()
}

/**
 * 编辑成功事件
 */
const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  message.success('图片裁剪成功')
}

// ----- AI 扩图引用 -----
const imageOutPaintingRef = ref()

/**
 * 打开 AI 扩图弹窗
 */
const doImagePainting = () => {
  imageOutPaintingRef.value?.openModal()
}

/**
 * AI 扩图保存事件
 */
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  message.success('AI 扩图成功')
}

// 页面初始化
onMounted(() => {
  loadPictureData()
  getTagCategoryOptions()
})
</script>

<style scoped>
#editPicturePage {
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

.space-info {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 0;
}

.space-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.space-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 图片预览区域 */
.preview-section {
  margin-bottom: 32px;
}

.preview-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.preview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 16px;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  object-fit: contain;
}

/* 编辑区域和表单区域 */
.edit-section,
.form-section {
  margin-bottom: 32px;
}

.edit-card,
.form-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.edit-card:hover,
.form-card:hover {
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

.edit-actions {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 按钮样式 */
.edit-button {
  border-radius: 10px;
  border: 2px solid #e1e8ed;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 44px;
  padding: 0 24px;
}

.edit-button:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.ai-button {
  border-radius: 10px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  height: 44px;
  padding: 0 24px;
}

.ai-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
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
}

.modern-input :deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.modern-textarea :deep(.ant-input) {
  border-radius: 12px;
  border: 2px solid #e1e8ed;
  padding: 12px 16px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: rgba(248, 250, 252, 0.8);
  resize: vertical;
}

.modern-textarea :deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.modern-select :deep(.ant-select-selector) {
  border-radius: 12px !important;
  border: 2px solid #e1e8ed !important;
  padding: 8px 12px !important;
  background: rgba(248, 250, 252, 0.8) !important;
  transition: all 0.3s ease;
}

.modern-select :deep(.ant-select-focused .ant-select-selector) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
  background: white !important;
}

.form-actions {
  display: flex;
  justify-content: center;
  width: 100%;
}

.submit-button {
  border-radius: 12px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  height: 44px;
  padding: 0 32px;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
}

.cancel-button {
  border-radius: 12px;
  border: 2px solid #e1e8ed;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 44px;
  padding: 0 32px;
}

.cancel-button:hover {
  border-color: #d1d5db;
  color: #6b7280;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 加载和错误状态 */
.loading-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.loading-placeholder {
  width: 300px;
  height: 200px;
}

.error-section {
  margin: 40px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #editPicturePage {
    padding: 16px;
  }
  
  .page-container {
    max-width: 100%;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .edit-card,
  .form-card,
  .preview-card {
    padding: 24px 20px;
  }
  
  .edit-actions {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  
  .edit-button,
  .ai-button {
    width: 100%;
    max-width: 200px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .submit-button,
  .cancel-button {
    width: 100%;
  }
}
</style> 