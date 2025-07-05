<template>
  <!-- @author Kryos -->
  <div id="userProfilePage">
    <!-- 用户基本信息卡片 -->
    <a-card class="profile-card" :loading="loading">
      <div class="profile-header">
        <a-avatar :size="100" :src="userInfo.userAvatar" class="profile-avatar">
          <template #icon>
            <UserOutlined />
          </template>
        </a-avatar>
        <div class="profile-info">
          <h2 class="profile-name">{{ userInfo.userName || '未设置昵称' }}</h2>
          <p class="profile-desc">{{ userInfo.userProfile || '这个用户很懒，什么都没有写~' }}</p>
          <div class="profile-tags">
            <a-tag v-if="userInfo.userRole === 'admin'" color="red">管理员</a-tag>
            <a-tag v-else-if="userInfo.userRole === 'vip'" color="gold">VIP会员</a-tag>
            <a-tag v-else color="blue">普通用户</a-tag>
          </div>
        </div>
      </div>
    </a-card>

    <!-- 功能菜单 -->
    <div class="function-menu">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8">
          <a-card hoverable class="function-card" @click="goToEdit">
            <div class="function-content">
              <div class="function-icon">
                <UserOutlined />
              </div>
              <div class="function-info">
                <h3>编辑资料</h3>
                <p>修改个人信息</p>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-card hoverable class="function-card" @click="goToSecurity">
            <div class="function-content">
              <div class="function-icon">
                <SafetyOutlined />
              </div>
              <div class="function-info">
                <h3>账户安全</h3>
                <p>修改密码</p>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-card hoverable class="function-card" @click="goToVip">
            <div class="function-content">
              <div class="function-icon">
                <CrownOutlined />
              </div>
              <div class="function-info">
                <h3>会员中心</h3>
                <p>兑换会员码</p>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-card hoverable class="function-card" @click="goToMySpace">
            <div class="function-content">
              <div class="function-icon">
                <FolderOutlined />
              </div>
              <div class="function-info">
                <h3>我的空间</h3>
                <p>管理空间</p>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined, SafetyOutlined, CrownOutlined, FolderOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getCurrentUserProfileUsingGet } from '@/api/userController.ts'

const router = useRouter()

// 用户信息
const userInfo = ref<API.UserVO>({
  id: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
  createTime: '',
})

// 加载状态
const loading = ref(false)

/**
 * 获取用户信息
 */
const fetchUserProfile = async () => {
  loading.value = true
  try {
    const res = await getCurrentUserProfileUsingGet()
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
    } else {
      message.error('获取用户信息失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取用户信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到编辑资料页面
 */
const goToEdit = () => {
  router.push('/user/edit')
}

/**
 * 跳转到账户安全页面
 */
const goToSecurity = () => {
  router.push('/user/security')
}

/**
 * 跳转到会员中心
 */
const goToVip = () => {
  router.push('/user_exchange_vip')
}

/**
 * 跳转到我的空间
 */
const goToMySpace = () => {
  router.push('/my_space')
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
#userProfilePage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px;
}

.profile-avatar {
  flex-shrink: 0;
  border: 3px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #1f2937;
}

.profile-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.profile-tags {
  display: flex;
  gap: 8px;
}

.function-menu {
  margin-top: 24px;
}

.function-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}

.function-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.function-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.function-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.function-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #1f2937;
}

.function-info p {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .profile-info {
    text-align: center;
  }
  
  .function-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}
</style>