<template>
  <div class="card order-card" @click="$emit('click')">
    <div class="head">
      <span class="number">订单号 {{ order.number }}</span>
      <span class="status" :style="{ color: orderStatusColor(order.status) }">
        {{ orderStatusText(order.status, order.deliveryType) }}
      </span>
    </div>
    <div class="type-row" v-if="order.orderType !== 1">
      <van-tag type="primary" plain>{{ orderTypeText(order.orderType) }}</van-tag>
      <van-tag v-if="order.deliveryType === 2" type="success" plain>社区自提</van-tag>
    </div>
    <div class="items">
      <div v-for="d in order.orderDetailList || []" :key="d.id" class="item">
        <van-image :src="imgUrl(d.image)" width="44" height="44" radius="4" fit="cover" />
        <span class="name ellipsis-2">{{ d.name }}</span>
        <span class="qty">{{ d.weight ? d.weight + '斤/kg' : 'x' + d.number }}</span>
        <span class="amt">¥{{ d.amount }}</span>
      </div>
    </div>
    <!-- 待自提突出显示自提码 -->
    <div v-if="order.status === 4 && order.deliveryType === 2 && order.pickupCode" class="pickup-code">
      自提码 <b>{{ order.pickupCode }}</b>
    </div>
    <div class="foot">
      <span class="total">实付 <span class="price">¥{{ order.amount }}</span></span>
      <div class="actions" @click.stop>
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { imgUrl, orderStatusText, orderStatusColor, orderTypeText } from '../utils/format'

defineProps({ order: { type: Object, required: true } })
defineEmits(['click'])
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
}
.status {
  font-weight: 600;
}
.type-row {
  margin-top: 6px;
  display: flex;
  gap: 6px;
}
.items {
  margin-top: 8px;
  border-top: 1px solid #f5f5f5;
  padding-top: 8px;
}
.item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
}
.item .name {
  flex: 1;
}
.qty {
  color: #999;
  font-size: 12px;
}
.amt {
  color: #333;
}
.pickup-code {
  margin-top: 6px;
  background: #f0f9eb;
  color: #39b57f;
  text-align: center;
  border-radius: 6px;
  padding: 8px;
  font-size: 14px;
}
.pickup-code b {
  font-size: 26px;
  letter-spacing: 4px;
  margin-left: 6px;
}
.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.actions {
  display: flex;
  gap: 8px;
}
</style>
