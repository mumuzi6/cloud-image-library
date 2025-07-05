<template>
  <!-- @author Kryos -->
  <div id="userSecurityPage">
    <div class="security-header">
      <h2>账户安全</h2>
      <a-button @click="goBack" type="text">
        <ArrowLeftOutlined />
        返回
      </a-button>
    </div>

    <a-row :gutter="24">
      <!-- 修改密码 -->
      <a-col :xs="24" :lg="16">
        <a-card title="修改密码" class="security-card">
          <a-form 
            ref="passwordFormRef"
            :model="passwordForm" 
            layout="vertical"
          >
            <a-form-item 
              label="当前密码" 
              name="oldPassword"
              :rules="[
                { required: true, message: '请输入当前密码' },
                { min: 8, message: '密码长度不能小于8位' }
              ]"
            >
              <a-input-password 
                v-model:value="passwordForm.oldPassword" 
                placeholder="请输入当前密码"
                class="modern-input"
              />
            </a-form-item>

            <a-form-item 
              label="新密码" 
              name="newPassword"
              :rules="[
                { required: true, message: '请输入新密码' },
                { min: 8, message: '密码长度不能小于8位' }
              ]"
            >
              <a-input-password 
                v-model:value="passwordForm.newPassword" 
                placeholder="请输入新密码"
                class="modern-input"
              />
            </a-form-item>

            <a-form-item 
              label="确认新密码" 
              name="confirmPassword"
              :rules="[
                { required: true, message: '请确认新密码' },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password 
                v-model:value="passwordForm.confirmPassword" 
                placeholder="请再次输入新密码"
                class="modern-input"
              />
            </a-form-item>

            <!-- 密码强度提示 -->
            <div class="password-tips">
              <h4>密码要求：</h4>
              <ul>
                <li>长度至少8位</li>
                <li>包含大写字母（推荐）</li>
                <li>包含小写字母（推荐）</li>
                <li>包含数字（推荐）</li>
              </ul>
            </div>

            <a-form-item>
              <a-button 
                type="primary" 
                size="large"
                :loading="passwordLoading"
                block
                @click="handlePasswordChange"
              >
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <!-- 安全信息 -->
      <a-col :xs="24" :lg="8">
        <a-card title="安全信息" class="security-info-card">
          <div class="security-info">
            <div class="info-item">
              <div class="info-label">账号安全等级</div>
              <div class="info-value">
                <a-progress 
                  :percent="securityLevel" 
                  :stroke-color="getSecurityColor()"
                  size="small"
                />
                <span class="security-text">{{ getSecurityText() }}</span>
              </div>
            </div>

            <div class="info-item">
              <div class="info-label">最后登录时间</div>
              <div class="info-value">{{ formatDate(userInfo.updateTime) }}</div>
            </div>

            <div class="info-item">
              <div class="info-label">账号创建时间</div>
              <div class="info-value">{{ formatDate(userInfo.createTime) }}</div>
            </div>
          </div>

          <a-divider />

          <div class="security-tips">
            <h4>安全建议：</h4>
            <ul>
              <li>定期更换密码</li>
              <li>使用强密码</li>
              <li>不要在公共场所登录</li>
              <li>及时退出登录</li>
            </ul>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getLoginUserUsingGet, updatePasswordUsingPost } from '@/api/userController.ts'

const router = useRouter()

// 密码表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 用户信息
const userInfo = ref<API.LoginUserVO>({
  id: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
  createTime: '',
  updateTime: '',
})

// 加载状态
const passwordLoading = ref(false)

// 表单引用
const passwordFormRef = ref()

// 安全等级（模拟计算）
const securityLevel = computed(() => {
  let level = 30 // 基础分数
  if (userInfo.value.userAvatar) level += 20 // 有头像
  if (userInfo.value.userProfile) level += 20 // 有简介
  if (userInfo.value.userRole === 'vip') level += 30 // VIP用户
  return Math.min(level, 100)
})

/**
 * 获取用户信息
 */
const fetchUserInfo = async () => {
  try {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
    } else {
      message.error('获取用户信息失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取用户信息失败，请稍后重试')
  }
}

/**
 * 确认密码校验
 */
const validateConfirmPassword = (_: any, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请确认新密码'))
  }
  if (value !== passwordForm.newPassword) {
    return Promise.reject(new Error('两次输入的密码不一致'))
  }
  return Promise.resolve()
}

/**
 * 修改密码
 */
const handlePasswordChange = async () => {
  try {
    // 手动触发表单验证
    await passwordFormRef.value?.validateFields()
    
    passwordLoading.value = true
    
    // 调用修改密码的API
    const res = await updatePasswordUsingPost({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
    })
    
    if (res.data.code === 0) {
      message.success('密码修改成功！')
      
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value?.resetFields()
    } else {
      message.error('密码修改失败：' + res.data.message)
    }
    
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单验证失败
      message.error('请完善密码信息')
      return
    }
    
    console.error('密码修改失败:', error)
    message.error('密码修改失败，请稍后重试')
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 获取安全等级颜色
 */
const getSecurityColor = () => {
  if (securityLevel.value < 50) return '#f5222d'
  if (securityLevel.value < 80) return '#faad14'
  return '#52c41a'
}

/**
 * 获取安全等级文本
 */
const getSecurityText = () => {
  if (securityLevel.value < 50) return '较低'
  if (securityLevel.value < 80) return '中等'
  return '较高'
}

/**
 * 格式化日期
 */
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString('zh-CN')
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
#userSecurityPage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.security-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.security-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.security-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.security-info-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
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

.modern-input :deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
  border: 1px solid #e1e8ed;
  padding: 12px;
  transition: all 0.3s ease;
}

.modern-input :deep(.ant-input-affix-wrapper:focus-within) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.password-tips {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  border-left: 4px solid #667eea;
}

.password-tips h4 {
  margin: 0 0 8px 0;
  color: #374151;
  font-size: 14px;
  font-weight: 600;
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #6b7280;
  font-size: 13px;
}

.password-tips li {
  margin-bottom: 4px;
}

.security-info {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #6b7280;
  font-size: 14px;
}

.info-value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
  text-align: right;
  flex: 1;
  margin-left: 16px;
}

.security-text {
  margin-left: 8px;
  font-size: 12px;
}

.security-tips {
  background: #f0f9ff;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #0ea5e9;
}

.security-tips h4 {
  margin: 0 0 8px 0;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.security-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #475569;
  font-size: 13px;
}

.security-tips li {
  margin-bottom: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .security-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-value {
    text-align: left;
    margin-left: 0;
  }
}
</style>