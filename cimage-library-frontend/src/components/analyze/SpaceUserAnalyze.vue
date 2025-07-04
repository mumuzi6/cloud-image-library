<template>
  <div class="space-user-analyze">
    <a-card title="用户上传分析">
      <v-chart :option="options" style="height: 320px; max-width: 100%" :loading="loading" />
      <template #extra>
        <a-space>
          <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions" />
          <a-input-search placeholder="请输入用户 id" enter-button="搜索用户" @search="doSearch" />
        </a-space>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: string
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 时间维度选项
const timeDimension = ref<'day' | 'week' | 'month'>('day')
// 分段选择器组件的选项
const timeDimensionOptions = [
  {
    label: '日',
    value: 'day',
  },
  {
    label: '周',
    value: 'week',
  },
  {
    label: '月',
    value: 'month',
  },
]
// 用户选项
const userId = ref<string>()
const doSearch = (value: string) => {
  userId.value = value
}

// 图表数据
const dataList = ref<API.SpaceUserAnalyzeResponse[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const res = await getSpaceUserAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    timeDimension: timeDimension.value,
    userId: userId.value ? Number(userId.value) : undefined,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

/**
 * 监听变量，参数改变时触发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})

// 图表选项
const options = computed(() => {
  const periods = dataList.value.map((item: any) => item.period) // 时间区间
  const counts = dataList.value.map((item: any) => item.count) // 上传数量

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: '时间区间' },
    yAxis: { type: 'value', name: '上传数量' },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        emphasis: {
          focus: 'series',
        },
      },
    ],
  }
})
</script>

<style scoped>
.space-user-analyze {
  height: 100%;
}

.space-user-analyze :deep(.ant-card) {
  height: 100%;
}

.space-user-analyze :deep(.ant-card-head) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.space-user-analyze :deep(.ant-card-head-title) {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.space-user-analyze :deep(.ant-card-extra) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.space-user-analyze :deep(.ant-card-body) {
  padding: 20px;
}

.space-user-analyze :deep(.ant-segmented) {
  font-size: 12px;
}

.space-user-analyze :deep(.ant-input-search) {
  max-width: 200px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .space-user-analyze :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .space-user-analyze :deep(.ant-card-head-title) {
    font-size: 14px;
  }
  
  .space-user-analyze :deep(.ant-card-extra) {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .space-user-analyze :deep(.ant-input-search) {
    max-width: 100%;
    width: 100%;
  }
  
  .space-user-analyze :deep(.ant-card-body) {
    padding: 16px;
  }
}
</style>
