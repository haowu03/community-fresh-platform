<template>
  <div class="page">
    <NavBar title="评价商品" />
    <div v-for="item in items" :key="item.productId" class="card">
      <div class="goods">
        <van-image :src="imgUrl(item.image)" width="48" height="48" radius="4" fit="cover" />
        <span class="name ellipsis-2">{{ item.name }}</span>
      </div>
      <div class="rate-row">
        <span>评分</span>
        <van-rate v-model="item.score" :size="22" color="#ffd21e" />
      </div>
      <van-field
        v-model="item.content"
        type="textarea"
        rows="3"
        maxlength="200"
        show-word-limit
        placeholder="分享你的使用感受（选填）"
        class="content"
      />
    </div>

    <div class="submit-wrap" v-if="items.length">
      <van-button round block type="success" :loading="submitting" @click="onSubmit">提交评价</van-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { orderDetail, submitEvaluation } from '../../api/order'
import { imgUrl } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const orderId = route.query.orderId
const items = ref([])
const submitting = ref(false)

// 逐个商品提交评分与文字
async function onSubmit() {
  submitting.value = true
  try {
    for (const it of items.value) {
      await submitEvaluation({
        orderId: Number(orderId),
        productId: it.productId,
        score: it.score,
        content: it.content,
        images: '[]'
      })
    }
    showToast('评价成功')
    router.replace('/order')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const detail = await orderDetail(orderId)
  items.value = (detail.orderDetailList || []).map(d => ({
    productId: d.productId,
    name: d.name,
    image: d.image,
    score: 5,
    content: ''
  }))
})
</script>

<style scoped>
.goods {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name {
  font-size: 14px;
  font-weight: 500;
}
.rate-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px 0;
  font-size: 14px;
}
.content {
  background: #f7f8fa;
  border-radius: 6px;
}
.submit-wrap {
  padding: 16px;
}
</style>
