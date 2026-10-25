<template>
  <!-- 单图上传组件：v-model 绑定后端返回的相对路径 -->
  <el-upload
    class="img-uploader"
    :show-file-list="false"
    accept="image/*"
    :http-request="doUpload"
  >
    <img v-if="modelValue" :src="imgUrl(modelValue)" class="img-preview" />
    <el-icon v-else class="img-uploader-icon"><Plus /></el-icon>
  </el-upload>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { uploadFile } from '../api/common'
import { imgUrl } from '../utils'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

// 自定义上传：调后端 /admin/common/upload
async function doUpload({ file }) {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片不能超过 5MB')
    return
  }
  const res = await uploadFile(file)
  emit('update:modelValue', res.data)
  ElMessage.success('上传成功')
}
</script>

<style scoped>
.img-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s;
}

.img-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.img-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}

.img-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
</style>
