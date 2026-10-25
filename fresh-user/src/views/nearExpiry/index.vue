<template>
  <div class="page">
    <NavBar title="临期特价" />
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <div class="prod-grid">
        <ProductCard v-for="p in list" :key="p.batchId || p.id" :product="p" @click="$router.push(`/detail/${p.id}`)">
          <template #price>
            <span>
              <span class="price">¥{{ p.discountPrice }}</span>
              <span class="price-del">¥{{ p.price }}</span>
            </span>
          </template>
          <div class="expiry">
            <van-tag type="danger">剩 {{ p.remainDays }} 天</van-tag>
            <span class="date">{{ fmtDate(p.expiryDate) }} 到期</span>
          </div>
        </ProductCard>
      </div>
      <van-empty v-if="finished && !list.length" description="暂无临期特价商品" />
    </van-list>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import NavBar from '../../components/NavBar.vue'
import ProductCard from '../../components/ProductCard.vue'
import { nearExpiryList } from '../../api/product'
import { fmtDate } from '../../utils/format'

const list = ref([])
const loading = ref(false)
const finished = ref(false)

async function loadData() {
  try {
    list.value = (await nearExpiryList()) || []
  } finally {
    loading.value = false
    finished.value = true
  }
}
</script>

<style scoped>
.prod-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 10px 12px;
}
.expiry {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.date {
  font-size: 11px;
  color: #999;
}
</style>
