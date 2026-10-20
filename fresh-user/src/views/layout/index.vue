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
