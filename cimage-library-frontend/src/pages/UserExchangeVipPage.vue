<template>
  <!-- @author Kryos -->
  <div id="vipExchangePage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1>💎 会员码兑换</h1>
          <p>输入您的会员兑换码，立即升级为VIP会员</p>
        </div>
        <div class="header-decoration">
          <div class="decoration-circle"></div>
          <div class="decoration-circle"></div>
          <div class="decoration-circle"></div>
        </div>
      </div>
    </div>

    <!-- 兑换表单卡片 -->
    <div class="exchange-card">
      <div class="card-header">
        <h3>🎫 兑换码输入</h3>
        <span class="card-subtitle">请输入您获得的会员兑换码</span>
      </div>
      
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item name="vipCode" label="兑换码">
          <a-input
            v-model:value="formData.vipCode"
            placeholder="请输入会员兑换码"
            allow-clear
            size="large"
            class="exchange-input"
          />
        </a-form-item>
        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            size="large"
            class="exchange-button"
            :loading="loading"
          >
            <template #icon>
              <span v-if="!loading">✨</span>
            </template>
            {{ loading ? '兑换中...' : '立即兑换' }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 会员权益说明卡片 -->
    <div class="benefits-card">
      <div class="card-header">
        <h3>👑 VIP会员权益</h3>
        <span class="card-subtitle">升级VIP后您将享受以下特权</span>
      </div>
      
      <div class="benefits-grid">
        <div class="benefit-item">
          <div class="benefit-icon">🚀</div>
          <div class="benefit-content">
            <h4>高速上传</h4>
            <p>享受更快的图片上传速度</p>
          </div>
        </div>
        <div class="benefit-item">
          <div class="benefit-icon">☁️</div>
          <div class="benefit-content">
            <h4>扩容存储</h4>
            <p>获得更大的云存储空间</p>
          </div>
        </div>
        <div class="benefit-item">
          <div class="benefit-icon">🎨</div>
          <div class="benefit-content">
            <h4>高级功能</h4>
            <p>解锁更多图片编辑功能</p>
          </div>
        </div>
        <div class="benefit-item">
          <div class="benefit-icon">🎯</div>
          <div class="benefit-content">
            <h4>优先支持</h4>
            <p>享受优先客服支持服务</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { exchangeVipUsingPost } from '@/api/userController.ts'
import { useRouter } from 'vue-router'

// 表单数据
const formData = reactive<API.VipExchangeRequest>({
  vipCode: '',
})

// 提交任务状态
const loading = ref(false)

const router = useRouter()

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 校验兑换码是否为空
  if (!formData.vipCode) {
    message.error('请输入兑换码')
    return
  }

  loading.value = true

  try {
    // 调用兑换 API
    const res = await exchangeVipUsingPost({
      vipCode: formData.vipCode,
    })

    // 操作成功
    if (res.data.code === 0 && res.data.data) {
      message.success('兑换成功！')
      // 跳转到主页或其他页面
      router.push({
        path: `/`,
      })
    } else {
      message.error('兑换失败：' + res.data.message)
    }
  } catch (error) {
    message.error('兑换失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#vipExchangePage {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 页面头部样式 */
.page-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-title h1 {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-title p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.header-decoration {
  display: flex;
  gap: 12px;
}

.decoration-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  animation: float 3s ease-in-out infinite;
}

.decoration-circle:nth-child(2) {
  animation-delay: -1s;
}

.decoration-circle:nth-child(3) {
  animation-delay: -2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

/* 卡片通用样式 */
.exchange-card,
.benefits-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.exchange-card:hover,
.benefits-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  margin-bottom: 24px;
  text-align: center;
}

.card-header h3 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.card-subtitle {
  font-size: 14px;
  color: #7f8c8d;
}

/* 兑换表单样式 */
.exchange-input {
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
}

.exchange-input:focus,
.exchange-input:hover {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.exchange-button {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.exchange-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.exchange-button:active {
  transform: translateY(0);
}

/* 会员权益样式 */
.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 24px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.benefit-item:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.benefit-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  flex-shrink: 0;
}

.benefit-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.benefit-content p {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #vipExchangePage {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px 20px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-title h1 {
    font-size: 24px;
  }
  
  .exchange-card,
  .benefits-card {
    padding: 24px 20px;
  }
  
  .benefits-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .benefit-item {
    padding: 16px;
  }
  
  .header-decoration {
    display: none;
  }
}

@media (max-width: 480px) {
  .benefit-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .benefit-icon {
    width: 50px;
    height: 50px;
    font-size: 24px;
  }
}
</style>
