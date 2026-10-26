<template>
  <van-nav-bar :title="title" left-arrow fixed placeholder @click-left="back">
    <template #left>
      <van-icon name="arrow-left" size="20" />
      <span v-if="backText" class="back-text">{{ backText }}</span>
    </template>
    <template #right><slot name="right" /></template>
  </van-nav-bar>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({ title: { type: String, default: '' }, backText: { type: String, default: '' } })
const router = useRouter()
const back = () => router.back()
</script>

<style scoped>
:deep(.van-nav-bar) {
  background: linear-gradient(180deg, rgba(247, 243, 234, 0.96), rgba(247, 243, 234, 0.88));
  backdrop-filter: blur(6px);
}
:deep(.van-nav-bar__left) {
  color: var(--green-ink, #1e4d35);
}
.back-text {
  font-family: var(--font-kai, serif);
  font-size: 14px;
  letter-spacing: 0.1em;
  margin-left: 2px;
}
</style>