<template>
  <a-modal
    class="image-cropper"
    v-model:open="visible"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <!-- 图片裁切组件 -->
    <div class="cropper-container" :style="{ transform: `rotate(${currentRotation}deg)` }">
      <vue-cropper
        ref="cropperRef"
        :img="imageUrl"
        output-type="png"
        :info="true"
        :can-move-box="true"
        :fixed-box="false"
        :auto-crop="true"
        :center-box="true"
        :fixed="fixedCrop"
        :fixed-number="fixedNumber"
      />
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 协同编辑操作 -->
    <div class="image-edit-actions" v-if="isTeamSpace">
      <a-space>
        <a-button v-if="editingUser" disabled>{{ editingUser.userName }} 正在编辑</a-button>
        <a-button v-if="canEnterEdit" type="primary" ghost @click="enterEdit">进入编辑</a-button>
        <a-button v-if="canExitEdit" danger ghost @click="exitEdit">退出编辑</a-button>
      </a-space>
    </div>
    <div style="margin-bottom: 16px" />
    <!-- 扩展功能区域 -->
    <div class="image-cropper-extended-controls">
      <!-- 裁剪比例控制 -->
      <div class="control-group">
        <label>裁剪比例:</label>
        <a-space>
          <a-switch v-model:checked="fixedCrop" :disabled="!canEdit">
            <template #checkedChildren>固定</template>
            <template #unCheckedChildren>自由</template>
          </a-switch>
          <a-select 
            v-model:value="selectedRatio" 
            :disabled="!canEdit || !fixedCrop"
            @change="onRatioChange"
            style="width: 100px"
          >
            <a-select-option value="16:9">16:9</a-select-option>
            <a-select-option value="4:3">4:3</a-select-option>
            <a-select-option value="1:1">1:1</a-select-option>
            <a-select-option value="3:2">3:2</a-select-option>
            <a-select-option value="9:16">9:16</a-select-option>
          </a-select>
        </a-space>
      </div>
      
      <!-- 任意角度旋转 -->
      <div class="control-group">
        <label>旋转角度:</label>
        <a-space>
          <a-input-number 
            v-model:value="customAngle" 
            :min="-360" 
            :max="360" 
            :step="1"
            :disabled="!canEdit"
            style="width: 80px"
          />
          <a-button @click="rotateCustom" :disabled="!canEdit">旋转</a-button>
        </a-space>
      </div>
      
      <!-- 等比例放大 -->
      <div class="control-group">
        <label>等比例放大:</label>
        <a-space>
          <a-input-number 
            v-model:value="scaleRatio" 
            :min="0.1" 
            :max="5" 
            :step="0.1"
            :disabled="!canEdit"
            style="width: 80px"
          />
          <a-button @click="scaleToRatio" :disabled="!canEdit">应用</a-button>
        </a-space>
      </div>
    </div>
    
    <div style="margin-bottom: 16px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft" :disabled="!canEdit">向左旋转</a-button>
        <a-button @click="rotateRight" :disabled="!canEdit">向右旋转</a-button>
        <a-button @click="changeScale(1)" :disabled="!canEdit">放大</a-button>
        <a-button @click="changeScale(-1)" :disabled="!canEdit">缩小</a-button>
        <a-button type="primary" :loading="loading" :disabled="!canEdit" @click="handleConfirm"
          >确认
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed, onUnmounted, ref, watch, watchEffect } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import PictureEditWebSocket from '@/utils/pictureEditWebSocket.ts'
import { PICTURE_EDIT_ACTION_ENUM, PICTURE_EDIT_MESSAGE_TYPE_ENUM } from '@/constants/picture.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  spaceId?: string
  space?: API.SpaceVO
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 是否为团队空间
const isTeamSpace = computed(() => {
  return props.space?.spaceType === SPACE_TYPE_ENUM.TEAM
})

// 获取图片裁切器的引用
const cropperRef = ref()

// 扩展功能相关的响应式变量
const fixedCrop = ref(false)  // 是否固定比例
const fixedNumber = ref([16, 9])  // 固定比例数组
const selectedRatio = ref('16:9')  // 选中的比例
const customAngle = ref(0)  // 自定义旋转角度
const scaleRatio = ref(1)  // 等比例放大倍数
const currentRotation = ref(0)  // 当前累积旋转角度

// 缩放比例
const changeScale = (num: number) => {
  cropperRef.value?.changeScale(num)
  if (num > 0) {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_IN)
  } else {
    editAction(PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT)
  }
}

// 向左旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
  currentRotation.value = (currentRotation.value - 90) % 360
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT)
}

// 向右旋转
const rotateRight = () => {
  cropperRef.value.rotateRight()
  currentRotation.value = (currentRotation.value + 90) % 360
  editAction(PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT)
}

// 扩展功能方法
// 比例选择改变时的处理
const onRatioChange = (value: string) => {
  const ratios: Record<string, number[]> = {
    '16:9': [16, 9],
    '4:3': [4, 3],
    '1:1': [1, 1],
    '3:2': [3, 2],
    '9:16': [9, 16]
  }
  fixedNumber.value = ratios[value] || [16, 9]
  
  // 需要重新应用比例设置
  if (fixedCrop.value && cropperRef.value) {
    // 先清除当前裁剪框
    cropperRef.value.clearCrop()
    // 等待一个tick后重新生成裁剪框以应用新的比例
    setTimeout(() => {
      if (cropperRef.value) {
        cropperRef.value.goAutoCrop()
      }
    }, 10)
  }
}

// 自定义角度旋转
const rotateCustom = () => {
  if (customAngle.value !== 0) {
    const rotationAngle = customAngle.value
    // 更新当前旋转角度
    currentRotation.value = (currentRotation.value + rotationAngle) % 360
    // 重置输入框
    customAngle.value = 0
    // 发送 WebSocket 消息时包含角度信息
    editActionWithData(PICTURE_EDIT_ACTION_ENUM.ROTATE_CUSTOM, { rotationAngle })
  }
}

// 等比例放大到指定倍数
const scaleToRatio = () => {
  if (scaleRatio.value > 0) {
    // 先重置到原始大小，然后按比例放大
    cropperRef.value?.changeScale(scaleRatio.value)
    editAction(PICTURE_EDIT_ACTION_ENUM.SCALE_RATIO)
  }
}

// 确认裁切
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已经裁切好的文件
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    // 上传图片
    handleUpload({ file })
  })
}

const loading = ref(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  loading.value = false
}

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
  // 断开 WebSocket 连接
  if (websocket) {
    websocket.disconnect()
  }
  editingUser.value = undefined
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

// --------- 实时编辑 ---------
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser

// 正在编辑的用户
const editingUser = ref<API.UserVO>()
// 当前用户是否可进入编辑
const canEnterEdit = computed(() => {
  return !editingUser.value
})
// 正在编辑的用户是本人，可退出编辑
const canExitEdit = computed(() => {
  return editingUser.value?.id === loginUser.id
})
// 可以点击编辑图片的操作按钮
const canEdit = computed(() => {
  // 不是团队空间，默认就可以编辑
  if (!isTeamSpace.value) {
    return true
  }
  // 团队空间，只有编辑者才能协同编辑
  return editingUser.value?.id === loginUser.id
})

// 编写 WebSocket 逻辑
let websocket: PictureEditWebSocket | null

// 初始化 WebSocket 连接，绑定监听事件
const initWebsocket = () => {
  const pictureId = props.picture?.id
  if (!pictureId || !visible.value) {
    return
  }
  // 防止之前的连接未释放
  if (websocket) {
    websocket.disconnect()
  }
  // 创建 websocket 实例
  websocket = new PictureEditWebSocket(pictureId)
  // 建立连接
  websocket.connect()

  // 监听一系列的事件
  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.INFO, (msg) => {
    console.log('收到通知消息：', msg)
    message.info(msg.message)
  })

  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ERROR, (msg) => {
    console.log('收到错误通知：', msg)
    message.info(msg.message)
  })

  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT, (msg) => {
    console.log('收到进入编辑状态的消息：', msg)
    message.info(msg.message)
    editingUser.value = msg.user
  })

  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION, (msg) => {
    console.log('收到编辑操作的消息：', msg)
    message.info(msg.message)
    // 根据收到的编辑操作，执行相应的操作
    switch (msg.editAction) {
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_LEFT:
        // 协同编辑时直接操作 cropper 和旋转状态，不触发 editAction
        cropperRef.value.rotateLeft()
        currentRotation.value = (currentRotation.value - 90) % 360
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_RIGHT:
        cropperRef.value.rotateRight()
        currentRotation.value = (currentRotation.value + 90) % 360
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_IN:
        changeScale(1)
        break
      case PICTURE_EDIT_ACTION_ENUM.ZOOM_OUT:
        changeScale(-1)
        break
      case PICTURE_EDIT_ACTION_ENUM.ROTATE_CUSTOM:
        // 协同编辑时的自定义旋转需要从消息中获取角度
        if (msg.rotationAngle) {
          currentRotation.value = (currentRotation.value + msg.rotationAngle) % 360
        }
        break
      case PICTURE_EDIT_ACTION_ENUM.SCALE_RATIO:
        scaleToRatio()
        break
    }
  })

  websocket.on(PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT, (msg) => {
    console.log('收到退出编辑状态的消息：', msg)
    message.info(msg.message)
    editingUser.value = undefined
  })
}

// 监听属性和 visible 变化，初始化 WebSocket 连接
watchEffect(() => {
  // 只有团队空间，才初始化 WebSocket 连接
  if (isTeamSpace.value) {
    initWebsocket()
  }
})

// 监听固定比例状态变化，重新应用裁剪设置
watch(fixedCrop, (newValue) => {
  if (cropperRef.value) {
    if (newValue) {
      // 开启固定模式时，先清除再重新生成
      cropperRef.value.clearCrop()
      setTimeout(() => {
        if (cropperRef.value) {
          cropperRef.value.goAutoCrop()
        }
      }, 10)
    } else {
      // 关闭固定模式时，重新生成自由裁剪框
      cropperRef.value.goAutoCrop()
    }
  }
})

// 监听比例数值变化，重新应用裁剪设置
watch(fixedNumber, () => {
  if (fixedCrop.value && cropperRef.value) {
    // 比例数值改变时，重新生成裁剪框
    cropperRef.value.clearCrop()
    setTimeout(() => {
      if (cropperRef.value) {
        cropperRef.value.goAutoCrop()
      }
    }, 10)
  }
}, { deep: true })

// 组件销毁时，断开 WebSocket 连接
onUnmounted(() => {
  // 断开 WebSocket 连接
  if (websocket) {
    websocket.disconnect()
  }
  editingUser.value = undefined
})

// 进入编辑状态
const enterEdit = () => {
  if (websocket) {
    // 发送进入编辑状态的请求
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.ENTER_EDIT,
    })
  }
}

// 退出编辑状态
const exitEdit = () => {
  if (websocket) {
    // 发送退出编辑状态的请求
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EXIT_EDIT,
    })
  }
}

// 编辑图片操作
const editAction = (action: string) => {
  if (websocket) {
    // 发送编辑操作的请求
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION,
      editAction: action,
    })
  }
}

// 编辑图片操作（带额外数据）
const editActionWithData = (action: string, data: any) => {
  if (websocket) {
    // 发送编辑操作的请求
    websocket.sendMessage({
      type: PICTURE_EDIT_MESSAGE_TYPE_ENUM.EDIT_ACTION,
      editAction: action,
      ...data,
    })
  }
}
</script>

<style>
.image-cropper {
  text-align: center;
}

.image-cropper .vue-cropper {
  height: 400px !important;
}

.cropper-container {
  transition: transform 0.3s ease;
  transform-origin: center;
}

.image-cropper-extended-controls {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.control-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.control-group:last-child {
  margin-bottom: 0;
}

.control-group label {
  font-weight: 500;
  color: #333;
  min-width: 80px;
  text-align: left;
}
</style>
