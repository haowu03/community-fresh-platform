<template>
  <div class="page">
    <NavBar title="我的订单" />
    <van-tabs v-model:active="active" color="#39b57f" @change="reset" sticky offset-top="46">
      <van-tab v-for="t in tabs" :key="t.title" :title="t.title" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="reset">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
        <OrderCard v-for="o in list" :key="o.id" :order="o" @click="$router.push(`/orderDetail/${o.id}`)">
          <template #actions>
            <!-- 待付款：去支付 / 取消 -->
            <template v-if="o.status === 1">
              <van-button size="small" type="success" round @click="onPay(o)">去支付</van-button>
              <van-button size="small" round @click="onCancel(o)">取消</van-button>
            </template>
            <!-- 待接单/备货中：催单 -->
            <van-button v-else-if="o.status === 2 || o.status === 3" size="small" round @click="onReminder(o)">催单</van-button>
            <!-- 已完成：再来一单 / 去评价 -->
            <template v-else-if="o.status === 5">
              <van-button size="small" round @click="onRepetition(o)">再来一单</van-button>
              <van-button size="small" type="warning" round @click="$router.push(`/evaluation?orderId=${o.id}`)">去评价</van-button>
            </template>
          </template>
        </OrderCard>
        <van-empty v-if="finished && !list.length" description="暂无订单" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import OrderCard from '../../components/OrderCard.vue'
import { historyOrders, payOrder, cancelOrder, repetitionOrder, reminderOrder } from '../../api/order'
import { useCartStore } from '../../stores/cart'

const cart = useCartStore()

// 状态 tab：进行中 = 待接单(2) + 备货中(3)，分两次请求合并
const tabs = [
  { title: '全部', status: null },
  { title: '待付款', status: 1 },
  { title: '进行中', statusList: [2, 3] },
  { title: '待自提', status: 4 },
  { title: '已完成', status: 5 }
]

const active = ref(0)
const list = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const PAGE_SIZE = 10

async function loadData() {
  const tab = tabs[active.value]
  try {
    if (tab.statusList) {
      // 进行中：合并两个状态的首页数据
      const [r1, r2] = await Promise.all(
        tab.statusList.map(s => historyOrders({ page: 1, pageSize: 20, status: s }))
      )
      const merged = [...((r1 && r1.records) || []), ...((r2 && r2.records) || [])]
      merged.sort((a, b) => String(b.orderTime).localeCompare(String(a.orderTime)))
      list.value.push(...merged)
      finished.value = true
    } else {
      const data = await historyOrders({ page: page.value, pageSize: PAGE_SIZE, status: tab.status || undefined })
      const records = (data && data.records) || []
      list.value.push(...records)
      page.value += 1
      if (records.length < PAGE_SIZE) finished.value = true
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 切换 tab / 下拉刷新：重置列表
function reset() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = true
  loadData()
}

// 模拟支付
async function onPay(o) {
  await showConfirmDialog({ title: '模拟支付', message: `订单金额 ¥${o.amount}，确认支付？`, confirmButtonText: '立即支付' })
  await payOrder({ orderNumber: o.number, payMethod: 1 })
  showToast('支付成功')
  reset()
}

// 取消订单
async function onCancel(o) {
  await showConfirmDialog({ title: '提示', message: '确定取消该订单吗？' })
  await cancelOrder(o.id)
  showToast('已取消')
  reset()
}

// 催单
async function onReminder(o) {
  await reminderOrder(o.id)
  showToast('已催单，商家会尽快处理')
}

// 再来一单：回写购物车
async function onRepetition(o) {
  await repetitionOrder(o.id)
  showToast('已加入购物车')
  cart.refresh()
}
</script>
