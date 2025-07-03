<template>
  <div id="userLoginPage">
    <div class="login-container">
      <div class="login-card">
        <div class="header-section">
          <h2 class="title">K智协图汇 - 用户登录</h2>
          <div class="desc">企业级智能协同云图库</div>
        </div>
        
        <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
          <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input 
              v-model:value="formState.userAccount" 
              placeholder="请输入账号" 
              size="large"
              class="modern-input"
            />
          </a-form-item>
          <a-form-item
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码长度不能小于 8 位' },
            ]"
          >
            <a-input-password 
              v-model:value="formState.userPassword" 
              placeholder="请输入密码" 
              size="large"
              class="modern-input"
            />
          </a-form-item>
          
          <div class="tips">
            没有账号？
            <RouterLink to="/user/register" class="link">去注册</RouterLink>
          </div>
          
          <a-form-item>
            <a-button 
              type="primary" 
              html-type="submit" 
              size="large"
              class="login-button"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 用于接受表单输入的值
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLoginUsingPost(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

#userLoginPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.2);
}

.header-section {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  text-shadow: none;
}

.desc {
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
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

.modern-input :deep(.ant-input-password) {
  border-radius: 12px;
  border: 2px solid #e1e8ed;
  background: rgba(248, 250, 252, 0.8);
  transition: all 0.3s ease;
}

.modern-input :deep(.ant-input-password:focus-within) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.modern-input :deep(.ant-input-password .ant-input) {
  border: none;
  box-shadow: none;
  background: transparent;
  padding: 12px 16px;
  font-size: 16px;
}

.tips {
  text-align: center;
  font-size: 14px;
  margin-bottom: 24px;
  color: #6b7280;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  background: linear-gradient(45deg, #5a6fd8, #6a4190);
}

.login-button:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    padding: 32px 24px;
    margin: 16px;
  }
  
  .title {
    font-size: 1.5rem;
  }
}
</style>
