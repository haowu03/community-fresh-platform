<template>
  <div class="login-page">
    <div class="logo">
      <van-icon name="shop-o" size="52" color="#39b57f" />
      <h2>社区生鲜</h2>
      <p>新鲜直达 · 社区自提 · 溯源安心</p>
    </div>

    <van-tabs v-model:active="active" animated color="#39b57f">
      <!-- 登录 -->
      <van-tab title="登录">
        <van-form class="form" @submit="onLogin">
          <van-field
            v-model="loginForm.username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请输入账号' }]"
          />
          <van-field
            v-model="loginForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
          <div class="btn">
            <van-button round block type="success" native-type="submit" :loading="loading">登 录</van-button>
          </div>
          <p class="tip">演示账号：user1 / 123456（普通用户）　leader1 / 123456（团长）</p>
        </van-form>
      </van-tab>

      <!-- 注册 -->
      <van-tab title="注册">
        <van-form class="form" @submit="onRegister">
          <van-field
            v-model="regForm.username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请输入账号' }]"
          />
          <van-field
            v-model="regForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
            v-model="regForm.confirm"
            type="password"
            name="confirm"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[
              { required: true, message: '请再次输入密码' },
              { validator: v => v === regForm.password, message: '两次密码不一致' }
            ]"
          />
          <van-field v-model="regForm.name" name="name" label="昵称" placeholder="请输入昵称（选填）" />
          <van-field v-model="regForm.phone" name="phone" label="手机号" placeholder="请输入手机号（选填）" />
          <div class="btn">
            <van-button round block type="success" native-type="submit" :loading="loading">注 册</van-button>
          </div>
        </van-form>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login, register } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { useCartStore } from '../../stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const active = ref(0)
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', confirm: '', name: '', phone: '' })

// 登录：保存 token + 用户信息后跳转来源页
async function onLogin() {
  loading.value = true
  try {
    const data = await login({ username: loginForm.username, password: loginForm.password })
    userStore.setLogin(data)
    cartStore.refresh()
    showToast('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

// 注册：成功后切回登录页签
async function onRegister() {
  loading.value = true
  try {
    await register({
      username: regForm.username,
      password: regForm.password,
      name: regForm.name,
      phone: regForm.phone
    })
    showToast('注册成功，请登录')
    loginForm.username = regForm.username
    active.value = 0
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #fff;
}
.logo {
  text-align: center;
  padding: 48px 0 24px;
}
.logo h2 {
  margin: 8px 0 4px;
  color: #333;
}
.logo p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.form {
  padding: 16px;
}
.btn {
  margin: 24px 16px 0;
}
.tip {
  margin-top: 16px;
  text-align: center;
  color: #999;
  font-size: 12px;
}
</style>
