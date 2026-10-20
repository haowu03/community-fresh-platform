<template>
  <div class="page">
    <NavBar title="团购详情" />
    <div v-if="g">
      <van-image :src="imgUrl(g.productImage)" width="100%" height="280" fit="cover" />
      <div class="card">
        <div class="name">{{ g.productName }}</div>
        <div class="price-row">
          <span class="price big">¥{{ g.groupPrice }}</span>
          <span class="price-del">¥{{ g.originalPrice }}</span>
          <van-tag :color="groupStatusMap[g.status] && groupStatusMap[g.status].color">{{ g.statusDesc }}</van-tag>
        </div>
        <van-progress :percentage="g.progress || 0" stroke-width="8" color="#39b57f" class="progress" />
        <div class="meta">{{ g.minNum }}人成团 · 已团 {{ g.joinedNum }} 人 · 剩余 {{ g.remainNum }} 名额</div>
      </div>

      <div class="card">
        <van-cell-group :border="false">
          <van-cell title="发起团长" :value="g.leaderName" />
          <van-cell title="自提点" :value="g.pointName" />
          <van-cell title="开始时间" :value="fmtTime(g.startTime)" />
          <van-cell title="截止时间" :value="fmtTime(g.endTime)" />
        </van-cell-group>
      </div>

      <div class="bottom-placeholder" />
      <van-action-bar>
        <van-action-bar-icon icon="shop-o" text="首页" @click="$router.push('/home')" />
        <van-action-bar-button
          type="success"
          :text="g.status === 1 ? '立即参团' : g.statusDesc"
          :disabled="g.status !== 1"
          @click="showBuy = true"
        />
      </van-action-bar>

      <!-- 参团数量弹层 -->
      <van-popup v-model:show="showBuy" position="bottom" round>
        <div class="buy-panel">
          <div class="head">
            <van-image :src="imgUrl(g.productImage)" width="56" height="56" radius="6" fit="cover" />
            <div>
              <div class="name">{{ g.productName }}</div>
              <div class="price">¥{{ g.groupPrice }}</div>
            </div>
          </div>
          <div class="row">
            <span>参团份数</span>
            <van-stepper v-model="number" min="1" />
          </div>
          <van-button round block type="success" @click="goSubmit">去下单</van-button>
        </div>
      </van-popup>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { groupBuyDetail } from '../../api/groupbuy'
import { imgUrl, fmtTime, groupStatusMap } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const g = ref(null)
const showBuy = ref(false)
const number = ref(1)
const id = route.params.id

// 团购下单：orderType=3，提交订单页固定自提点，仅选时间段
function goSubmit() {
  showBuy.value = false
  router.push({
    path: '/submit',
    query: { orderType: 3, groupActivityId: id, productId: g.value.productId, number: number.value }
  })
}

onMounted(async () => {
  g.value = await groupBuyDetail(id)
})
</script>

<style scoped>
.name {
  font-size: 17px;
  font-weight: 700;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 6px;
}
.big {
  font-size: 24px;
}
.progress {
  margin-top: 12px;
}
.meta {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
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
</style>
