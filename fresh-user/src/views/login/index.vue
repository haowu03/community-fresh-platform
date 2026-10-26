<template>
  <div class="login-page">
    <div class="logo">
      <div class="logo-stamp"><van-icon name="shop-o" size="30" /></div>
      <h2 class="kai">社区生鲜</h2>
      <p>新鲜直达 · 社区自提 · 溯源安心</p>
    </div>

    <van-tabs v-model:active="active" animated>
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
            <van-button round block type="primary" native-type="submit" :loading="loading">登 录</van-button>
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
            <van-button round block type="primary" native-type="submit" :loading="loading">注 册</van-button>
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
  background:
    radial-gradient(rgba(30, 77, 53, 0.045) 1px, transparent 1.2px) 0 0 / 22px 22px,
    var(--paper, #f7f3ea);
}
.logo {
  text-align: center;
  padding: 52px 0 26px;
  position: relative;
}
.logo-stamp {
  width: 62px;
  height: 62px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--paper, #f7f3ea);
  background: var(--green-ink, #1e4d35);
  border-radius: 8px;
  transform: rotate(-4deg);
  box-shadow: 4px 5px 0 rgba(30, 77, 53, 0.16);
}
.logo h2 {
  margin: 18px 0 6px;
  font-size: 28px;
  font-weight: 700;
  color: var(--green-ink, #1e4d35);
  letter-spacing: 0.3em;
  text-indent: 0.3em;
}
.logo p {
  margin: 0;
  color: var(--green-moss, #4c7b58);
  font-size: 11px;
  letter-spacing: 0.22em;
}
:deep(.van-tabs__wrap) {
  padding: 0 34px;
}
:deep(.van-tabs__nav) {
  background: transparent;
}
:deep(.van-tab) {
  font-family: var(--font-kai, serif);
  font-size: 15px;
  letter-spacing: 0.2em;
  color: var(--ink-soft, #6f7267);
}
:deep(.van-tabs__line) {
  background: linear-gradient(90deg, var(--tomato, #d8402b), var(--yellow-ochre, #e9a13b));
  width: 24px !important;
  border-radius: 2px;
}
.form {
  padding: 12px 26px;
}
:deep(.van-cell) {
  background: rgba(255, 255, 255, 0.6);
  border-bottom: 1px dashed rgba(30, 77, 53, 0.16);
  margin-bottom: 6px;
  border-radius: 6px;
  overflow: hidden;
}
:deep(.van-cell::after) {
  display: none;
}
:deep(.van-field__label) {
  color: var(--green-ink, #1e4d35);
  font-family: var(--font-kai, serif);
  letter-spacing: 0.15em;
}
.btn {
  margin: 26px 10px 0;
}
.btn :deep(.van-button) {
  letter-spacing: 0.4em;
  text-indent: 0.4em;
  font-family: var(--font-kai, serif);
  box-shadow: 0 4px 12px rgba(30, 77, 53, 0.28);
}
.tip {
  margin-top: 18px;
  text-align: center;
  color: var(--ink-soft, #6f7267);
  font-size: 11px;
  letter-spacing: 0.05em;
}
</style>
