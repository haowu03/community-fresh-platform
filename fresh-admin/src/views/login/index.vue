<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="login-title">生鲜配送平台 · 管理端</h2>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="onLogin">
          登 录
        </el-button>
      </el-form>
      <p class="login-tip">演示账号：admin / 123456</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../../api/employee'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 登录：保存 token + 姓名后跳首页
async function onLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f4037 0%, #99f2c8 100%);
}

.login-card {
  width: 400px;
  padding: 16px 8px;
}

.login-title {
  text-align: center;
  margin-bottom: 24px;
  color: #303133;
}

.login-btn {
  width: 100%;
}

.login-tip {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 16px;
}
</style>
