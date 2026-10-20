<template>
  <div class="page">
    <NavBar title="订单详情" />
    <div v-if="order">
      <!-- 状态 -->
      <div class="card status-card" :style="{ background: orderStatusColor(order.status) }">
        <div class="status-text">{{ orderStatusText(order.status, order.deliveryType) }}</div>
        <div class="status-sub">{{ orderTypeText(order.orderType) }} · {{ order.deliveryType === 2 ? '社区自提' : '配送到家' }}</div>
      </div>

      <!-- 自提码（大号突出） -->
      <div v-if="order.deliveryType === 2 && order.pickupCode" class="card code-card">
        <div class="code-label">取货请出示自提码</div>
        <div class="code">{{ order.pickupCode }}</div>
        <div v-if="order.timeSlotDesc" class="code-sub">自提时段 {{ order.timeSlotDesc }}</div>
      </div>

      <!-- 履约信息 -->
      <div class="card">
        <div class="sec-title">{{ order.deliveryType === 2 ? '自提信息' : '收货信息' }}</div>
        <van-cell-group :border="false">
          <template v-if="order.deliveryType === 2">
            <van-cell title="自提点" :value="order.pickupPointName || '-'" />
            <van-cell title="自提时段" :value="order.timeSlotDesc || '-'" />
            <van-cell title="自提地址" :label="order.address || '-'" />
            <van-cell title="联系电话" :value="order.phone || '-'" />
          </template>
          <template v-else>
            <van-cell title="收货人" :value="`${order.consignee || '-'}　${order.phone || ''}`" />
            <van-cell title="收货地址" :label="order.address || '-'" />
          </template>
        </van-cell-group>
      </div>

      <!-- 商品明细 -->
      <div class="card">
        <div class="sec-title">商品明细</div>
        <div v-for="d in order.orderDetailList || []" :key="d.id" class="goods">
          <van-image :src="imgUrl(d.image)" width="48" height="48" radius="4" fit="cover" />
          <span class="name ellipsis-2">{{ d.name }}</span>
          <span class="qty">{{ d.weight ? d.weight + '斤/kg' : 'x' + d.number }}</span>
          <span class="amt">¥{{ d.amount }}</span>
        </div>
        <van-cell title="打包费" :value="`¥${order.packAmount || 0}`" :border="false" />
        <van-cell title="实付金额" :border="false">
          <template #value><span class="price">¥{{ order.amount }}</span></template>
        </van-cell>
        <van-cell v-if="order.remark" title="备注" :value="order.remark" :border="false" />
      </div>

      <!-- 订单时间线 -->
      <div class="card">
        <div class="sec-title">订单信息</div>
        <van-cell-group :border="false">
          <van-cell title="订单号" :value="order.number" />
          <van-cell title="下单时间" :value="fmtTime(order.orderTime)" />
          <van-cell v-if="order.checkoutTime" title="支付时间" :value="fmtTime(order.checkoutTime)" />
          <van-cell v-if="order.estimatedDeliveryTime" title="预计送达/可自提" :value="fmtTime(order.estimatedDeliveryTime)" />
          <van-cell v-if="order.deliveryTime" title="完成时间" :value="fmtTime(order.deliveryTime)" />
          <van-cell v-if="order.pickupTime" title="核销时间" :value="fmtTime(order.pickupTime)" />
          <van-cell v-if="order.cancelTime" title="取消时间" :value="fmtTime(order.cancelTime)" />
          <van-cell v-if="order.cancelReason" title="取消原因" :value="order.cancelReason" />
        </van-cell-group>
      </div>

      <div class="bottom-placeholder" />

      <!-- 底部操作 -->
      <van-action-bar v-if="order.status === 1 || order.status === 5">
        <van-action-bar-button v-if="order.status === 1" type="success" text="去支付" @click="onPay" />
        <van-action-bar-button v-if="order.status === 1" type="danger" text="取消订单" @click="onCancel" />
        <van-action-bar-button v-if="order.status === 5" type="success" text="再来一单" @click="onRepetition" />
        <van-action-bar-button v-if="order.status === 5" type="warning" text="去评价" @click="$router.push(`/evaluation?orderId=${order.id}`)" />
      </van-action-bar>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { orderDetail, payOrder, cancelOrder, repetitionOrder } from '../../api/order'
import { useCartStore } from '../../stores/cart'
import { imgUrl, fmtTime, orderStatusText, orderStatusColor, orderTypeText } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const order = ref(null)
const id = route.params.id

async function load() {
  order.value = await orderDetail(id)
}

async function onPay() {
  await showConfirmDialog({ title: '模拟支付', message: `订单金额 ¥${order.value.amount}，确认支付？`, confirmButtonText: '立即支付' })
  await payOrder({ orderNumber: order.value.number, payMethod: 1 })
  showToast('支付成功')
  load()
}

async function onCancel() {
  await showConfirmDialog({ title: '提示', message: '确定取消该订单吗？' })
  await cancelOrder(id)
  showToast('已取消')
  load()
}

async function onRepetition() {
  await repetitionOrder(id)
  showToast('已加入购物车')
  cart.refresh()
  router.push('/cart')
}

onMounted(load)
</script>

<style scoped>
.status-card {
  color: #fff;
  text-align: center;
  padding: 24px 12px;
}
.status-text {
  font-size: 20px;
  font-weight: 700;
}
.status-sub {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
}
.code-card {
  text-align: center;
}
.code-label {
  font-size: 13px;
  color: #666;
}
.code {
  font-size: 40px;
  font-weight: 700;
  letter-spacing: 8px;
  color: #39b57f;
  margin: 8px 0;
}
.code-sub {
  font-size: 12px;
  color: #999;
}
.sec-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}
.goods {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}
.goods .name {
  flex: 1;
}
.qty {
  color: #999;
  font-size: 12px;
}
</style>
