<template>
  <div class="page">
    <NavBar title="提交订单" />

    <!-- 履约方式（团购单强制自提，隐藏切换） -->
    <div class="card" v-if="orderType !== 3">
      <van-tabs v-model:active="deliveryTab" color="#39b57f" @change="onDeliveryChange">
        <van-tab title="配送到家" />
        <van-tab title="社区自提" />
      </van-tabs>
    </div>
    <div class="card fixed-tip" v-else>
      <van-icon name="location-o" color="#39b57f" />
      团购订单统一配送至活动自提点：{{ activity && activity.pointName }}
    </div>

    <!-- 配送：选择收货地址 -->
    <div class="card" v-if="deliveryType === 1">
      <div class="sec-title">收货地址</div>
      <van-radio-group v-model="addressId">
        <van-cell v-for="a in addresses" :key="a.id" clickable @click="addressId = a.id">
          <template #title>
            <span class="addr-name">{{ a.consignee }} {{ a.phone }}</span>
            <van-tag v-if="a.isDefault === 1" type="success" class="tag">默认</van-tag>
            <div class="addr-detail">{{ a.provinceName }}{{ a.cityName }}{{ a.districtName }}{{ a.detail }}</div>
          </template>
          <template #right-icon><van-radio :name="a.id" checked-color="#39b57f" /></template>
        </van-cell>
      </van-radio-group>
      <van-cell v-if="!addresses.length" title="暂无收货地址" is-link to="/address" title-style="color:#ee0a24" />
      <van-cell v-else title="管理地址" is-link to="/address" />
    </div>

    <!-- 自提：选择自提点 + 时间段 -->
    <div class="card" v-if="deliveryType === 2">
      <div class="sec-title">自提点</div>
      <!-- 团购：自提点固定为活动自提点 -->
      <van-cell v-if="orderType === 3" :title="activity && activity.pointName" :label="activity && activity.productName" />
      <van-radio-group v-else v-model="pointId">
        <van-cell v-for="p in points" :key="p.id" clickable @click="pointId = p.id; onPointChange(p.id)">
          <template #title>{{ p.name }}</template>
          <template #label>{{ p.provinceName }}{{ p.cityName }}{{ p.districtName }}{{ p.detailAddress }}　{{ p.businessHours }}</template>
          <template #right-icon><van-radio :name="p.id" checked-color="#39b57f" /></template>
        </van-cell>
      </van-radio-group>

      <div class="sec-title slot-title">自提时间段</div>
      <div class="slots">
        <van-tag
          v-for="s in slots"
          :key="s.id"
          size="large"
          :type="slotId === s.id ? 'primary' : 'default'"
          :class="{ disabled: isFull(s), on: slotId === s.id }"
          @click="selectSlot(s)"
        >
          {{ fmtSlot(s.startTime) }}~{{ fmtSlot(s.endTime) }}{{ isFull(s) ? '（已满）' : '' }}
        </van-tag>
        <span v-if="!slots.length && pointId" class="empty-tip">该自提点暂无可用时间段</span>
      </div>
    </div>

    <!-- 商品明细 -->
    <div class="card">
      <div class="sec-title">商品明细</div>
      <div v-for="(d, i) in items" :key="i" class="goods">
        <van-image :src="imgUrl(d.image)" width="48" height="48" radius="4" fit="cover" />
        <span class="name ellipsis-2">{{ d.name }}</span>
        <span class="qty">{{ d.qtyText }}</span>
        <span class="amt">¥{{ d.amount }}</span>
      </div>
      <van-cell title="打包费" value="¥0.00" :border="false" />
      <van-field v-model="remark" label="备注" placeholder="请输入备注（选填）" :border="false" />
    </div>

    <div class="bottom-placeholder" />

    <!-- 底部提交栏 -->
    <van-submit-bar :price="totalFen" button-text="提交订单" button-color="#39b57f" :loading="submitting" @submit="onSubmit" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { addressList } from '../../api/address'
import { pickupPointList, pickupSlots } from '../../api/pickup'
import { presaleDetail } from '../../api/presale'
import { groupBuyDetail } from '../../api/groupbuy'
import { productDetail } from '../../api/product'
import { submitOrder, payOrder } from '../../api/order'
import { useCartStore } from '../../stores/cart'
import { imgUrl, fmtSlot } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()

// 下单参数（普通单走购物车；预售/团购单带参直购）
const orderType = Number(route.query.orderType || 1)
const presaleId = route.query.presaleId ? Number(route.query.presaleId) : null
const groupActivityId = route.query.groupActivityId ? Number(route.query.groupActivityId) : null
const productId = route.query.productId ? Number(route.query.productId) : null
const number = Number(route.query.number || 1)
const weight = route.query.weight ? Number(route.query.weight) : null

const deliveryTab = ref(orderType === 3 ? 1 : 0) // tab 索引：0配送 1自提
const deliveryType = ref(orderType === 3 ? 2 : 1)
const addresses = ref([])
const addressId = ref(null)
const points = ref([])
const pointId = ref(null)
const slots = ref([])
const slotId = ref(null)
const remark = ref('')
const items = ref([]) // { name, image, qtyText, amount }
const total = ref(0)
const activity = ref(null) // 团购活动信息
const submitting = ref(false)

const totalFen = computed(() => Math.round(total.value * 100))

function onDeliveryChange(idx) {
  deliveryType.value = idx === 0 ? 1 : 2
}

function isFull(s) {
  return s.usedOrders != null && s.maxOrders != null && s.usedOrders >= s.maxOrders
}

function selectSlot(s) {
  if (isFull(s)) return
  slotId.value = s.id
}

// 切换自提点后加载时间段
async function onPointChange(id) {
  slotId.value = null
  slots.value = (await pickupSlots(id)) || []
}

// 初始化普通单（购物车商品）
async function initNormal() {
  await cart.refresh()
  if (!cart.list.length) {
    showToast('购物车为空')
    router.back()
    return
  }
  items.value = cart.list.map(i => ({
    name: i.name,
    image: i.image,
    qtyText: i.weight != null ? `称重 ${i.weight}` : `x${i.number}`,
    amount: Number(i.amount).toFixed(2)
  }))
  total.value = cart.list.reduce((s, i) => s + Number(i.amount || 0), 0)
}

// 初始化预售单
async function initPresale() {
  const [act, prod] = await Promise.all([presaleDetail(presaleId), productDetail(productId)])
  const qtyText = prod.priceType === 2 && weight ? `${weight}${prod.unit || '斤'}` : `x${number}`
  const amount = (Number(act.presalePrice) * (prod.priceType === 2 && weight ? weight : number)).toFixed(2)
  items.value = [{ name: act.productName, image: act.productImage, qtyText, amount }]
  total.value = Number(amount)
}

// 初始化团购单：强制自提，自提点取活动自提点
async function initGroupBuy() {
  const [act, prod] = await Promise.all([groupBuyDetail(groupActivityId), productDetail(productId)])
  activity.value = act
  const amount = (Number(act.groupPrice) * number).toFixed(2)
  items.value = [{ name: act.productName, image: act.productImage, qtyText: `x${number}`, amount }]
  total.value = Number(amount)
  pointId.value = act.pickupPointId
  await onPointChange(act.pickupPointId)
}

// 提交订单 → 模拟支付弹窗
async function onSubmit() {
  if (deliveryType.value === 1 && !addressId.value) {
    showToast('请选择收货地址')
    return
  }
  if (deliveryType.value === 2 && (!pointId.value || !slotId.value)) {
    showToast('请选择自提点和时间段')
    return
  }
  const payload = {
    deliveryType: deliveryType.value,
    orderType,
    remark: remark.value,
    payMethod: 1,
    packAmount: 0
  }
  if (deliveryType.value === 1) payload.addressBookId = addressId.value
  if (deliveryType.value === 2) {
    payload.pickupPointId = pointId.value
    payload.timeSlotId = slotId.value
  }
  if (orderType === 2) payload.presaleId = presaleId
  if (orderType === 3) payload.groupActivityId = groupActivityId
  if (orderType !== 1) {
    payload.productId = productId
    payload.number = number
    if (weight) payload.weight = weight
  }

  submitting.value = true
  try {
    const vo = await submitOrder(payload)
    cart.refresh()
    try {
      await showConfirmDialog({
        title: '模拟支付',
        message: `订单金额 ¥${vo.orderAmount}，确认支付？`,
        confirmButtonText: '立即支付',
        cancelButtonText: '稍后支付'
      })
      await payOrder({ orderNumber: vo.orderNumber, payMethod: 1 })
      showToast('支付成功')
    } catch (e) {
      // 取消支付或支付失败，跳订单列表查看待付款
    }
    router.replace('/order')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 地址与自提点
  if (orderType !== 3) {
    addressList().then(list => {
      addresses.value = list || []
      const def = addresses.value.find(a => a.isDefault === 1) || addresses.value[0]
      if (def) addressId.value = def.id
    }).catch(() => {})
    pickupPointList().then(list => { points.value = list || [] }).catch(() => {})
  }

  if (orderType === 2) await initPresale()
  else if (orderType === 3) await initGroupBuy()
  else await initNormal()
})
</script>

<style scoped>
.sec-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}
.slot-title {
  margin-top: 12px;
}
.fixed-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #39b57f;
  font-size: 14px;
}
.addr-name {
  font-size: 14px;
  font-weight: 500;
}
.tag {
  margin-left: 6px;
}
.addr-detail {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.slots .van-tag {
  padding: 6px 12px;
}
.slots .van-tag.disabled {
  opacity: 0.4;
}
.empty-tip {
  font-size: 12px;
  color: #999;
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
