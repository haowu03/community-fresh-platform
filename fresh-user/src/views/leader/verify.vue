<template>
  <div class="page">
    <NavBar title="自提核销" />
    <div class="card verify-card">
      <van-icon name="certificate" size="48" color="#39b57f" />
      <p class="tip">请输入用户出示的 6 位自提码</p>
      <van-field
        v-model="pickupCode"
        type="digit"
        maxlength="6"
        placeholder="输入自提码"
        class="code-input"
      />
      <van-button round block type="success" :loading="loading" :disabled="pickupCode.length !== 6" @click="onVerify">
        确认核销
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { verifyPickup } from '../../api/groupbuy'

const pickupCode = ref('')
const loading = ref(false)

// 团长核销自提码（仅本自提点订单）
async function onVerify() {
  loading.value = true
  try {
    await verifyPickup({ pickupCode: pickupCode.value })
    showToast('核销成功')
    pickupCode.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.verify-card {
  text-align: center;
  padding: 32px 20px;
}
.tip {
  color: #666;
  font-size: 14px;
  margin: 12px 0 20px;
}
.code-input {
  background: #f5f6f8;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 24px;
  letter-spacing: 8px;
  text-align: center;
}
:deep(.code-input .van-field__control) {
  text-align: center;
  font-size: 24px;
  letter-spacing: 8px;
}
</style>
