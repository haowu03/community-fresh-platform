<template>
  <div class="page">
    <NavBar title="预售专区" />
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <div v-for="item in list" :key="item.id" class="card presale-card">
        <van-image :src="imgUrl(item.productImage)" width="96" height="96" radius="8" fit="cover" />
        <div class="info">
          <div class="name">{{ item.productName }}</div>
          <div class="price-row">
            <span class="price">¥{{ item.presalePrice }}</span>
            <span class="stock">剩余 {{ item.remainStock }} 份</span>
          </div>
          <div class="time">截止：{{ fmtTime(item.endTime) }}</div>
          <div class="time">发货：{{ fmtDate(item.deliveryDate) }}</div>
          <van-button size="small" type="success" round class="buy" @click="openBuy(item)">立即预订</van-button>
        </div>
      </div>
      <van-empty v-if="finished && !list.length" description="暂无进行中的预售活动" />
    </van-list>

    <!-- 选数量/重量弹层 -->
    <van-popup v-model:show="showBuy" position="bottom" round>
      <div class="buy-panel" v-if="current">
        <div class="head">
          <van-image :src="imgUrl(current.productImage)" width="56" height="56" radius="6" fit="cover" />
          <div>
            <div class="name">{{ current.productName }}</div>
            <div class="price">¥{{ current.presalePrice }}<i v-if="isWeigh" class="unit">/{{ unit }}</i></div>
          </div>
        </div>

        <!-- 按份：份数步进 -->
        <div v-if="!isWeigh" class="row">
          <span>预订份数</span>
          <van-stepper v-model="number" min="1" :max="current.remainStock || 1" />
        </div>

        <!-- 按重量：称重步进 + 快捷重量 -->
        <template v-else>
          <div class="row">
            <span>称重重量（{{ unit }}）</span>
            <van-stepper v-model="weight" min="0.5" step="0.5" :decimal-length="1" theme="round" button-size="26" />
          </div>
          <div class="weight-chips">
            <span
              v-for="w in [0.5, 1, 1.5, 2, 2.5, 3]"
              :key="w"
              class="w-chip"
              :class="{ on: Number(weight) === w }"
              @click="weight = w"
            >{{ w }}{{ unit }}</span>
          </div>
          <div class="weight-est">
            预估金额 <b class="price">¥{{ (Number(weight || 0) * current.presalePrice).toFixed(2) }}</b>
          </div>
        </template>

        <van-button round block type="success" @click="goSubmit">去下单</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { presaleList } from '../../api/presale'
import { productDetail } from '../../api/product'
import { imgUrl, fmtTime, fmtDate } from '../../utils/format'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const showBuy = ref(false)
const current = ref(null)
const product = ref(null) // 当前预售商品的详情（判断按份/按重）
const number = ref(1)
const weight = ref(1)

const isWeigh = computed(() => product.value && product.value.priceType === 2)
const unit = computed(() => (product.value && product.value.unit) || '斤')

async function loadData() {
  try {
    list.value = (await presaleList()) || []
  } finally {
    loading.value = false
    finished.value = true
  }
}

async function openBuy(item) {
  current.value = item
  number.value = 1
  weight.value = 1
  product.value = null
  showBuy.value = true
  try {
    product.value = await productDetail(item.productId)
  } catch (e) {
    showBuy.value = false
    showToast('商品信息加载失败')
  }
}

// 预售下单：携带 orderType=2 + presaleId + productId + number/weight 跳提交订单页
function goSubmit() {
  const query = {
    orderType: 2,
    presaleId: current.value.id,
    productId: current.value.productId,
    number: number.value
  }
  if (isWeigh.value) {
    if (!Number(weight.value) || Number(weight.value) <= 0) {
      showToast('请选择正确的重量')
      return
    }
    query.weight = Number(weight.value)
  }
  showBuy.value = false
  router.push({ path: '/submit', query })
}
</script>

<style scoped>
.presale-card {
  display: flex;
  gap: 10px;
}
.info {
  flex: 1;
  position: relative;
  min-width: 0;
}
.name {
  font-size: 15px;
  font-weight: 600;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 4px;
}
.price {
  font-size: 18px;
}
.stock {
  font-size: 12px;
  color: #ff976a;
}
.time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.buy {
  position: absolute;
  right: 0;
  bottom: 0;
}
.buy-panel {
  padding: 16px;
}
.head {
  display: flex;
  gap: 10px;
  align-items: center;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
}
.unit {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  font-style: normal;
}
.weight-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: -8px 0 12px;
}
.w-chip {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 14px;
  background: #f2f3f5;
  color: #555;
}
.w-chip.on {
  background: linear-gradient(135deg, #39b57f, #27a06c);
  color: #fff;
  font-weight: 600;
}
.weight-est {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
</style>
