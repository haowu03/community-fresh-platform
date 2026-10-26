<template>
  <div class="layout">
    <router-view />
    <van-tabbar route fixed placeholder>
      <van-tabbar-item to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/category" icon="apps-o">分类</van-tabbar-item>
      <van-tabbar-item to="/cart" icon="shopping-cart-o" :badge="cart.count || ''">购物车</van-tabbar-item>
      <van-tabbar-item to="/community" icon="chat-o">社区</van-tabbar-item>
      <van-tabbar-item to="/mine" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../../stores/cart'

const cart = useCartStore()
onMounted(() => cart.refresh())
</script>

<style scoped>
:deep(.van-tabbar) {
  height: 54px;
  background: rgba(247, 243, 234, 0.96);
  backdrop-filter: blur(8px);
}
:deep(.van-tabbar::before) {
  border-top: 1px dashed rgba(30, 77, 53, 0.18) !important;
}
:deep(.van-tabbar-item--active .van-tabbar-item__text) {
  font-family: var(--font-kai, serif);
  font-weight: 700;
  letter-spacing: 0.08em;
}
:deep(.van-tabbar-item__text) {
  font-size: 11px;
}
</style>
