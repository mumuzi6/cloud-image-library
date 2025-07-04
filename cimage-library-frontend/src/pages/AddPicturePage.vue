<template>
  <div id="addPicturePage">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h2 class="page-title">
          {{ route.query?.id ? '修改图片' : '创建图片' }}
        </h2>
        <a-typography-paragraph v-if="spaceId" type="secondary" class="space-info">
          保存至空间：<a :href="`/space/${spaceId}`" target="_blank" class="space-link">{{ spaceId }}</a>
        </a-typography-paragraph>
      </div>

      <!-- 上传选项卡 -->
      <div class="upload-section">
        <a-tabs v-model:activeKey="uploadType" class="modern-tabs">
          <a-tab-pane key="file" tab="文件上传">
            <div class="tab-content">
              <PictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
            </div>
          </a-tab-pane>
          <a-tab-pane key="url" tab="URL 上传" force-render>
            <div class="tab-content">
              <UrlPictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- 图片编辑工具栏 -->
      <div v-if="picture" class="edit-section">
        <div class="edit-card">
          <h3 class="section-title">图片编辑</h3>
          <a-space size="middle" class="edit-actions">
            <a-button 
              :icon="h(EditOutlined)" 
              @click="doEditPicture"
              class="edit-button"
              size="large"
            >
              编辑图片
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
              <a-button 
                type="primary" 
                html-type="submit" 
                size="large"
                class="submit-button"
              >
                {{ route.query?.id ? '保存修改' : '创建图片' }}
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, h, onMounted, reactive, ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'

const router = useRouter()
const route = useRoute()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploadType = ref<'file' | 'url'>('file')
// 空间 id
const spaceId = computed(() => {
  const id = route.query?.spaceId
  return typeof id === 'string' ? id : undefined
})

/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  console.log(values)
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    spaceId: spaceId.value,
    ...values,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}

const categoryOptions = ref<{value: string, label: string}[]>([])
const tagOptions = ref<{value: string, label: string}[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 获取老数据
const getOldPicture = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id && typeof id === 'string') {
    const res = await getPictureVoByIdUsingGet({
      id: id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})

// ----- 图片编辑器引用 ------
const imageCropperRef = ref()

// 编辑图片
const doEditPicture = async () => {
  imageCropperRef.value?.openModal()
}

// 编辑成功事件
const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// ----- AI 扩图引用 -----
const imageOutPaintingRef = ref()

// 打开 AI 扩图弹窗
const doImagePainting = async () => {
  imageOutPaintingRef.value?.openModal()
}

// AI 扩图保存事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// 获取空间信息
const space = ref<API.SpaceVO>()

// 获取空间信息
const fetchSpace = async () => {
  // 获取数据
  if (spaceId.value) {
    const res = await getSpaceVoByIdUsingGet({
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }
}

watchEffect(() => {
  fetchSpace()
})
</script>

<style scoped>
#addPicturePage {
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

/* 上传区域 */
.upload-section {
  margin-bottom: 32px;
}

.modern-tabs :deep(.ant-tabs-nav) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px 16px 0 0;
  padding: 0 24px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.modern-tabs :deep(.ant-tabs-tab) {
  border-radius: 8px;
  margin: 8px 4px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.modern-tabs :deep(.ant-tabs-tab:hover) {
  background: rgba(102, 126, 234, 0.1);
}

.modern-tabs :deep(.ant-tabs-tab-active) {
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
  color: #667eea;
  font-weight: 600;
}

.modern-tabs :deep(.ant-tabs-content-holder) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 0 0 16px 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-top: none;
}

.tab-content {
  padding: 24px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  #addPicturePage {
    padding: 16px;
  }
  
  .page-container {
    max-width: 100%;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .edit-card,
  .form-card {
    padding: 24px 20px;
  }
  
  .tab-content {
    padding: 20px 16px;
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
}
</style>
