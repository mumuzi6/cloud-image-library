<template>
  <div id="mySpacePage">
    <div class="loading-container">
      <div class="loading-card">
        <div class="loading-content">
          <div class="loading-icon">
            <a-spin size="large" />
          </div>
          <h2 class="loading-title">正在进入您的空间</h2>
          <p class="loading-text">请稍候，正在为您跳转到专属空间...</p>
          
          <div class="loading-steps">
            <div class="step-item" :class="{ active: step >= 1 }">
              <div class="step-icon">👤</div>
              <span class="step-text">验证用户身份</span>
            </div>
            <div class="step-item" :class="{ active: step >= 2 }">
              <div class="step-icon">🔍</div>
              <span class="step-text">查找空间信息</span>
            </div>
            <div class="step-item" :class="{ active: step >= 3 }">
              <div class="step-icon">🚀</div>
              <span class="step-text">准备进入空间</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted, ref } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const step = ref(0)

// 模拟加载步骤
const simulateLoadingSteps = () => {
  setTimeout(() => step.value = 1, 300)
  setTimeout(() => step.value = 2, 800)
  setTimeout(() => step.value = 3, 1300)
}

// 检查用户是否有个人空间
const checkUserSpace = async () => {
  simulateLoadingSteps()
  
  // 用户未登录，则直接跳转到登录页面
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    router.replace('/user/login')
    return
  }
  
  // 如果用户已登录，会获取该用户已创建的空间
  const res = await listSpaceVoByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
    spaceType: SPACE_TYPE_ENUM.PRIVATE,
  })
  
  if (res.data.code === 0) {
    // 如果有，则进入第一个空间
    if (res.data.data?.records && res.data.data.records.length > 0) {
      const space = res.data.data.records[0]
      // 添加延迟让用户看到完整的加载过程
      setTimeout(() => {
        router.replace(`/space/${space.id}`)
      }, 1800)
    } else {
      // 如果没有，则跳转到创建空间页面
      setTimeout(() => {
        router.replace('/add_space')
        message.warn('请先创建空间')
      }, 1800)
    }
  } else {
    message.error('加载我的空间失败，' + res.data.message)
  }
}

// 在页面加载时检查用户空间
onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped>
#mySpacePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

#mySpacePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.loading-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
}

.loading-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
  animation: cardFloat 3s ease-in-out infinite;
}

@keyframes cardFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.loading-icon {
  margin-bottom: 8px;
}

.loading-icon :deep(.ant-spin-dot) {
  font-size: 40px;
}

.loading-icon :deep(.ant-spin-dot-item) {
  background-color: #667eea;
}

.loading-title {
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  text-shadow: none;
}

.loading-text {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.loading-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  margin-top: 16px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.5);
  border: 1px solid #e1e8ed;
  transition: all 0.5s ease;
  opacity: 0.4;
  transform: translateX(-20px);
}

.step-item.active {
  opacity: 1;
  transform: translateX(0);
  background: linear-gradient(45deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-color: rgba(102, 126, 234, 0.3);
}

.step-icon {
  font-size: 18px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  flex-shrink: 0;
}

.step-item.active .step-icon {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.step-text {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.step-item.active .step-text {
  color: #2c3e50;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .loading-card {
    padding: 32px 24px;
    margin: 16px;
  }
  
  .loading-title {
    font-size: 1.5rem;
  }
  
  .step-item {
    padding: 10px 14px;
  }
  
  .step-icon {
    width: 28px;
    height: 28px;
    font-size: 16px;
  }
}
</style>
