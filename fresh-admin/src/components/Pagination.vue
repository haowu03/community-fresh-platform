<template>
  <!-- 统一分页组件：v-model:page / v-model:pageSize，change 时抛出 change 事件 -->
  <div class="pager-row">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page="page"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})
const emit = defineEmits(['update:page', 'update:pageSize', 'change'])

// 页码变化
function onPageChange(p) {
  emit('update:page', p)
  emit('change')
}

// 每页条数变化：回到第一页
function onSizeChange(s) {
  emit('update:pageSize', s)
  emit('update:page', 1)
  emit('change')
}
</script>
