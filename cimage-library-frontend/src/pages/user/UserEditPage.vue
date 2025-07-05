<template>
  <!-- @author Kryos -->
  <div id="userEditPage">
    <div class="edit-header">
      <h2>编辑个人资料</h2>
      <a-button @click="goBack" type="text">
        <ArrowLeftOutlined />
        返回
      </a-button>
    </div>

    <a-card class="edit-card">
      <a-form 
        ref="formRef"
        :model="formData" 
        layout="vertical"
      >
        <!-- 头像上传 -->
        <a-form-item label="头像">
          <div class="avatar-upload-container">
            <a-upload
              :show-upload-list="false"
              :before-upload="beforeUpload"
              @change="handleAvatarChange"
              accept="image/*"
              :disabled="avatarUploading"
            >
              <div class="avatar-upload-wrapper">
                <a-avatar :size="120" :src="formData.userAvatar" class="upload-avatar">
                  <template #icon>
                    <UserOutlined />
                  </template>
                </a-avatar>
                <div class="upload-overlay">
                  <div class="upload-content">
                    <UploadOutlined v-if="!avatarUploading" />
                    <a-spin v-else />
                    <div class="upload-text">{{ avatarUploading ? '上传中...' : '更换头像' }}</div>
                  </div>
                </div>
              </div>
            </a-upload>
            <div class="upload-tips">
              <p>支持jpg、jpeg、png、gif、bmp格式</p>
              <p>文件大小不超过5MB</p>
            </div>
          </div>
        </a-form-item>

        <!-- 基本信息 -->
        <a-form-item 
          label="昵称" 
          name="userName"
          :rules="[{ required: true, message: '请输入昵称' }]"
        >
          <a-input 
            v-model:value="formData.userName" 
            placeholder="请输入昵称"
            maxlength="20"
            show-count
            class="modern-input"
          />
        </a-form-item>

        <a-form-item 
          label="个人简介" 
          name="userProfile"
        >
          <a-textarea 
            v-model:value="formData.userProfile" 
            placeholder="写一段简介介绍自己吧..."
            :rows="4"
            maxlength="200"
            show-count
            class="modern-input"
          />
        </a-form-item>

        <!-- 提交按钮 -->
        <a-form-item>
          <a-space>
            <a-button 
              type="primary" 
              size="large"
              :loading="loading"
              @click="handleSubmit"
            >
              保存修改
            </a-button>
            <a-button 
              size="large"
              @click="resetForm"
            >
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined, UserOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getCurrentUserProfileUsingGet, updateCurrentUserProfileUsingPost, uploadUserAvatarUsingPost } from '@/api/userController.ts'

const router = useRouter()

// 表单数据
const formData = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

// 加载状态
const loading = ref(false)

// 头像上传状态
const avatarUploading = ref(false)

// 表单引用
const formRef = ref()

/**
 * 获取用户信息
 */
const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUserProfileUsingGet()
    if (res.data.code === 0 && res.data.data) {
      const userData = res.data.data
      formData.userName = userData.userName || ''
      formData.userAvatar = userData.userAvatar || ''
      formData.userProfile = userData.userProfile || ''
    } else {
      message.error('获取用户信息失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取用户信息失败，请稍后重试')
  }
}

/**
 * 上传前校验
 */
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过5MB!')
    return false
  }
  
  return true
}

/**
 * 头像上传处理
 */
const handleAvatarChange = async (info: any) => {
  const file = info.file.originFileObj || info.file
  
  if (!file || avatarUploading.value) {
    return
  }
  
  avatarUploading.value = true
  
  try {
    // 上传头像到服务器
    const res = await uploadUserAvatarUsingPost(file)
    
    if (res.data.code === 0 && res.data.data) {
      formData.userAvatar = res.data.data
      message.success('头像上传成功')
    } else {
      message.error('头像上传失败：' + res.data.message)
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    message.error('头像上传失败，请稍后重试')
    
    // 预览图片（备用方案）
    const reader = new FileReader()
    reader.onload = (e) => {
      formData.userAvatar = e.target?.result as string
    }
    reader.readAsDataURL(file)
  } finally {
    avatarUploading.value = false
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    // 手动触发表单验证
    await formRef.value?.validateFields()
    
    loading.value = true
    
    // 调用更新用户信息的API
    const res = await updateCurrentUserProfileUsingPost({
      userName: formData.userName,
      userAvatar: formData.userAvatar,
      userProfile: formData.userProfile,
    })
    
    if (res.data.code === 0) {
      message.success('个人资料更新成功！')
      
      // 返回用户中心
      router.push('/user/profile')
    } else {
      message.error('更新失败：' + res.data.message)
    }
    
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单验证失败
      message.error('请完善表单信息')
      return
    }
    
    console.error('更新失败:', error)
    message.error('更新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  fetchUserInfo()
  formRef.value?.resetFields()
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.go(-1)
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
#userEditPage {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.edit-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.edit-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-upload-wrapper {
  position: relative;
  cursor: pointer;
}

.upload-avatar {
  border: 3px solid #f0f0f0;
  transition: all 0.3s ease;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-upload-wrapper:hover .upload-overlay {
  opacity: 1;
}

.upload-content {
  color: white;
  text-align: center;
  font-size: 12px;
}

.upload-content .anticon {
  font-size: 20px;
  margin-bottom: 4px;
}

.upload-tips {
  color: #6b7280;
  font-size: 12px;
}

.upload-tips p {
  margin: 0;
  line-height: 1.4;
}

.modern-input :deep(.ant-input) {
  border-radius: 8px;
  border: 1px solid #e1e8ed;
  padding: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.modern-input :deep(.ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.modern-input :deep(.ant-input-count) {
  border-radius: 8px;
}

.modern-input :deep(.ant-input-count .ant-input) {
  border: 1px solid #e1e8ed;
  border-radius: 8px 8px 0 0;
}

.modern-input :deep(.ant-input-count .ant-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .avatar-upload-container {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .edit-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>