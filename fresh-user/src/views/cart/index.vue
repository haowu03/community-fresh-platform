<template>
  <div class="page">
    <van-nav-bar title="购物车" fixed placeholder>
      <template #right>
        <span v-if="cart.list.length" class="clean" @click="onClean">清空</span>
      </template>
    </van-nav-bar>

    <van-checkbox-group v-model="checkedIds">
      <div v-for="item in cart.list" :key="item.id" class="card cart-item">
        <van-checkbox :name="item.id" checked-color="#39b57f" />
        <van-image :src="imgUrl(item.image)" width="72" height="72" radius="6" fit="cover"
          @click="$router.push(`/detail/${item.productId}`)" />
        <div class="info">
          <div class="name ellipsis-2">{{ item.name }}</div>
          <div class="bottom">
            <span class="price">¥{{ item.amount }}</span>
            <!-- 按份商品：步进器改数量 -->
            <van-stepper
              v-if="item.weight == null"
              :model-value="item.number"
              min="1"
              theme="round"
              button-size="22"
              @plus="onPlus(item)"
              @minus="onMinus(item)"
            />
            <!-- 按重量商品：显示重量 + 删除 -->
            <div v-else class="weight-row">
              <span class="weight-text">称重 {{ item.weight }}</span>
              <van-button size="mini" plain type="danger" @click="onRemove(item)">删除</van-button>
            </div>
          </div>
        </div>
      </div>
    </van-checkbox-group>

    <van-empty v-if="!cart.list.length" description="购物车是空的，去逛逛吧">
      <van-button round type="success" size="small" @click="$router.push('/home')">去首页</van-button>
    </van-empty>

    <!-- 底部结算栏（位于 TabBar 之上） -->
    <div v-if="cart.list.length" class="settle-bar">
      <van-checkbox :model-value="allChecked" checked-color="#39b57f" @update:model-value="toggleAll">全选</van-checkbox>
      <div class="total">合计 <span class="price">¥{{ total }}</span></div>
      <van-button round type="success" :disabled="!checkedIds.length" @click="goSubmit">
        结算({{ checkedIds.length }})
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useCartStore } from '../../stores/cart'
import { addCart, subCart, cleanCart } from '../../api/cart'
import { imgUrl } from '../../utils/format'

const router = useRouter()
const cart = useCartStore()
const checkedIds = ref([])

// 合计金额（勾选项）
const total = computed(() =>
  cart.list
    .filter(i => checkedIds.value.includes(i.id))
    .reduce((sum, i) => sum + Number(i.amount || 0), 0)
    .toFixed(2)
)

const allChecked = computed(() => cart.list.length > 0 && checkedIds.value.length === cart.list.length)

// 全选 / 取消全选
function toggleAll(v) {
  checkedIds.value = v ? cart.list.map(i => i.id) : []
}

// 列表刷新后默认全选
watch(() => cart.list, list => {
  checkedIds.value = list.map(i => i.id)
})

// 按份 +1
async function onPlus(item) {
  await addCart({ productId: item.productId, number: 1 })
  cart.refresh()
}

// 按份 -1（减到 0 后端自动删除条目）
async function onMinus(item) {
  await subCart({ productId: item.productId })
  cart.refresh()
}

// 删除按重量条目
async function onRemove(item) {
  await subCart({ productId: item.productId, weight: item.weight })
  cart.refresh()
}

// 清空购物车
async function onClean() {
  await showConfirmDialog({ title: '提示', message: '确定清空购物车吗？' })
  await cleanCart()
  cart.refresh()
}

function goSubmit() {
  router.push('/submit')
}

onMounted(() => cart.refresh())
</script>

<style scoped>
.clean {
  color: #ee0a24;
  font-size: 14px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 14px;
  font-weight: 500;
}
.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.weight-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.weight-text {
  font-size: 12px;
  color: #999;
}
.settle-bar {
  position: fixed;
  left: 50%;
  right: auto;
  translate: -50% 0;
  width: 100%;
  max-width: 480px;
  box-sizing: border-box;
  bottom: 50px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
.total {
  flex: 1;
  text-align: right;
  font-size: 13px;
}
</style>
