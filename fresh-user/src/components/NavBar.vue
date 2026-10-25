<template>
  <van-nav-bar :title="title" left-arrow fixed placeholder @click-left="back">
    <template #right><slot name="right" /></template>
  </van-nav-bar>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({ title: { type: String, default: '' } })
const router = useRouter()
const back = () => router.back()
</script>
