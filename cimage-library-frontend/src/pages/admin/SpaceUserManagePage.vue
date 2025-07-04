<template>
  <div id="spaceUserManagePage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            👥 空间成员管理
          </h1>
          <p class="page-description">管理空间成员权限，协作更高效</p>
        </div>
        <div class="header-actions">
          <a-button type="primary" href="/add_space" target="_blank" class="action-btn">
            ➕ 创建空间
          </a-button>
          <a-button type="default" href="/space_analyze?queryPublic=1" target="_blank" class="action-btn">
            📊 分析公共图库
          </a-button>
          <a-button type="default" href="/space_analyze?queryAll=1" target="_blank" class="action-btn">
            📈 分析全部空间
          </a-button>
        </div>
      </div>
    </div>

    <!-- 添加成员表单 -->
    <div class="form-section">
      <div class="form-card">
        <div class="card-header">
          <h3 class="card-title">👤 添加新成员</h3>
          <p class="card-description">邀请用户加入空间协作</p>
        </div>
        <a-form layout="inline" :model="formData" @finish="handleSubmit" class="member-form">
          <a-form-item label="用户 ID" name="userId" class="form-item">
            <a-input 
              v-model:value="formData.userId" 
              placeholder="请输入用户 ID" 
              allow-clear 
              class="form-input"
              prefix="🔍"
            />
          </a-form-item>
          <a-form-item class="form-item">
            <a-button type="primary" html-type="submit" class="submit-btn">
              ➕ 添加用户
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>

    <!-- 成员列表 -->
    <div class="table-section">
      <div class="table-card">
        <div class="card-header">
          <h3 class="card-title">👥 成员列表</h3>
          <p class="card-description">当前空间的所有成员信息</p>
        </div>
        <a-table 
          :columns="columns" 
          :data-source="dataList" 
          class="members-table"
          :pagination="false"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'userInfo'">
              <div class="user-info">
                <a-avatar :src="record.user?.userAvatar" class="user-avatar">
                  {{ record.user?.userName?.charAt(0) }}
                </a-avatar>
                <div class="user-details">
                  <div class="user-name">{{ record.user?.userName }}</div>
                  <div class="user-id">ID: {{ record.user?.id }}</div>
                </div>
              </div>
            </template>
            <template v-if="column.dataIndex === 'spaceRole'">
              <a-select
                v-model:value="record.spaceRole"
                :options="SPACE_ROLE_OPTIONS"
                @change="(value) => editSpaceRole(value, record)"
                class="role-select"
              />
            </template>
            <template v-else-if="column.dataIndex === 'createTime'">
              <div class="time-info">
                <div class="time-date">{{ dayjs(record.createTime).format('YYYY-MM-DD') }}</div>
                <div class="time-hour">{{ dayjs(record.createTime).format('HH:mm:ss') }}</div>
              </div>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button 
                type="text" 
                danger 
                @click="doDelete(record.id)"
                class="delete-btn"
              >
                🗑️ 删除
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { SPACE_ROLE_OPTIONS } from '../../constants/space.ts'
import {
  addSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  listSpaceUserUsingPost,
} from '@/api/spaceUserController.ts'
import dayjs from 'dayjs'

interface Props {
  id: string
}

const props = defineProps<Props>()

const columns = [
  {
    title: '用户',
    dataIndex: 'userInfo',
  },
  {
    title: '角色',
    dataIndex: 'spaceRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 定义数据
const dataList = ref<API.SpaceUserVO[]>([])

// 获取数据
const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await listSpaceUserUsingPost({
    spaceId,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 添加成员表单
const formData = reactive<API.SpaceUserAddRequest>({})

// 创建成员
const handleSubmit = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await addSpaceUserUsingPost({
    spaceId,
    ...formData,
  })
  if (res.data.code === 0) {
    message.success('添加成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('添加失败，' + res.data.message)
  }
}

// 编辑成员角色
const editSpaceRole = async (value: any, record: any) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value,
  })
  if (res.data.code === 0) {
    message.success('修改成功')
  } else {
    message.error('修改失败，' + res.data.message)
  }
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}
</script>

<style scoped>
#spaceUserManagePage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 24px;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-info {
  flex: 1;
  min-width: 300px;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.page-description {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  border-radius: 8px;
  font-weight: 500;
  height: 40px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 表单区域 */
.form-section {
  margin-bottom: 24px;
}

.form-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  margin-bottom: 20px;
}

.card-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.card-description {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.member-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: end;
}

.form-item {
  margin-bottom: 0;
}

.form-input {
  width: 300px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  transition: all 0.3s ease;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.submit-btn {
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
  font-weight: 500;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 表格区域 */
.table-section {
  margin-bottom: 24px;
}

.table-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.members-table {
  margin-top: 16px;
}

.members-table :deep(.ant-table) {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
}

.members-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
  border: none;
  padding: 16px;
}

.members-table :deep(.ant-table-tbody > tr > td) {
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.members-table :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(102, 126, 234, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.user-id {
  font-size: 12px;
  color: #6b7280;
}

.role-select {
  width: 120px;
  border-radius: 6px;
}

.role-select :deep(.ant-select-selector) {
  border-radius: 6px;
  border-color: #d1d5db;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-date {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.time-hour {
  font-size: 12px;
  color: #6b7280;
}

.delete-btn {
  color: #ef4444;
  border-radius: 6px;
  padding: 4px 12px;
  height: auto;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #spaceUserManagePage {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-info {
    min-width: auto;
    width: 100%;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .action-btn {
    flex: 1;
    justify-content: center;
  }
  
  .form-card,
  .table-card {
    padding: 16px;
  }
  
  .member-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-input {
    width: 100%;
  }
  
  .submit-btn {
    width: 100%;
    justify-content: center;
  }
  
  .members-table :deep(.ant-table-thead > tr > th),
  .members-table :deep(.ant-table-tbody > tr > td) {
    padding: 12px 8px;
  }
  
  .user-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
  }
}
</style>