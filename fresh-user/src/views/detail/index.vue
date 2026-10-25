<template>
  <div class="page">
    <NavBar title="商品详情" />
    <div v-if="product">
      <!-- 商品大图 -->
      <van-swipe class="banner" :autoplay="3500" indicator-color="#39b57f">
        <van-swipe-item>
          <van-image :src="imgUrl(product.image)" fit="cover" @click="preview">
            <template #error>
              <div class="img-fallback"><van-icon name="photo-fail" size="40" /></div>
            </template>
          </van-image>
        </van-swipe-item>
      </van-swipe>

      <!-- 基本信息 -->
      <div class="card">
        <div class="price-row">
          <!-- 临期折扣氛围：折扣价 + 原价划线 + 折扣标签 -->
          <template v-if="product.discountPrice">
            <span class="price big">¥{{ product.discountPrice }}<i class="unit">/{{ product.unit || '份' }}</i></span>
            <span class="price-del">¥{{ product.price }}</span>
            <van-tag type="danger" round>限时折扣</van-tag>
          </template>
          <span v-else class="price big">¥{{ product.price }}<i class="unit">/{{ product.unit || '份' }}</i></span>
          <van-tag v-if="product.priceType === 2" type="warning" round>按重计价</van-tag>
        </div>
        <div class="name">{{ product.name }}</div>
        <div class="tags">
          <van-tag :color="freshType(product.freshType).color" plain>{{ freshType(product.freshType).text }}</van-tag>
          <span class="sales">已售{{ product.saleCount || 0 }}</span>
        </div>
        <van-cell-group :border="false" class="meta">
          <van-cell title="产地" :value="product.originPlace || '-'" />
          <van-cell title="保质期" :value="product.shelfLifeDays != null ? product.shelfLifeDays + ' 天' : '-'" />
          <van-cell title="库存" :value="String(product.stock ?? '-')" />
          <van-cell v-if="product.latestBatch && product.latestBatch.expiryDate" title="最新批次到期日" :value="fmtDate(product.latestBatch.expiryDate)" />
        </van-cell-group>
        <div v-if="product.description" class="desc">{{ product.description }}</div>
      </div>

      <!-- 溯源信息 -->
      <div class="card" v-if="trace">
        <div class="sec-title">溯源信息</div>
        <van-cell-group :border="false">
          <van-cell title="产地" :value="trace.originPlace || '-'" />
          <van-cell title="种植户/养殖户" :value="trace.farmer || '-'" />
          <van-cell title="种植日期" :value="fmtDate(trace.plantDate) || '-'" />
          <van-cell title="采摘/出栏日期" :value="fmtDate(trace.pickDate) || '-'" />
          <van-cell title="运输/冷链" :label="trace.transportInfo || '-'" />
        </van-cell-group>
        <div v-if="trace.testReport" class="report">
          <div class="report-label">检测报告</div>
          <van-image :src="imgUrl(trace.testReport)" width="100" height="100" fit="cover" radius="6"
            @click="showImagePreview({ images: [imgUrl(trace.testReport)], startPosition: 0 })" />
        </div>
      </div>

      <div class="bottom-placeholder" />

      <!-- 底部行动栏 -->
      <van-action-bar>
        <van-action-bar-icon icon="cart-o" text="购物车" :badge="cart.count || ''" @click="$router.push('/cart')" />
        <van-action-bar-icon icon="shop-o" text="首页" @click="$router.push('/home')" />
        <van-action-bar-button
          type="warning"
          :text="product.stock > 0 ? '加入购物车' : '已售罄'"
          :disabled="!(product.stock > 0)"
          @click="openAdd"
        />
      </van-action-bar>

      <!-- 加购弹层：按份用步进器，按重量输入重量 -->
      <van-popup v-model:show="showAdd" position="bottom" round>
        <div class="add-panel">
          <div class="add-head">
            <van-image :src="imgUrl(product.image)" width="64" height="64" radius="6" fit="cover" />
            <div>
              <div class="name">{{ product.name }}</div>
              <div class="price">¥{{ product.price }}<i class="unit">/{{ product.unit || '份' }}</i></div>
            </div>
          </div>

          <div v-if="product.priceType !== 2" class="row">
            <span>份数</span>
            <van-stepper v-model="number" min="1" :max="product.stock || 99" />
          </div>
          <template v-else>
            <div class="row">
              <span>称重重量（{{ product.unit || '斤' }}）</span>
              <van-stepper
                v-model="weight"
                min="0.5"
                step="0.5"
                :decimal-length="1"
                theme="round"
                button-size="26"
              />
            </div>
            <div class="weight-chips">
              <span
                v-for="w in [0.5, 1, 1.5, 2, 2.5, 3]"
                :key="w"
                class="w-chip"
                :class="{ on: Number(weight) === w }"
                @click="weight = w"
              >{{ w }}{{ product.unit || '斤' }}</span>
            </div>
            <div class="weight-est">
              预估金额 <b class="price">¥{{ (Number(weight || 0) * product.price).toFixed(2) }}</b>
            </div>
          </template>

          <van-button round block type="success" class="confirm" :loading="adding" @click="confirmAdd">
            加入购物车
          </van-button>
        </div>
      </van-popup>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showImagePreview, showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { productDetail, productTrace } from '../../api/product'
import { addCart } from '../../api/cart'
import { useCartStore } from '../../stores/cart'
import { imgUrl, freshType, fmtDate } from '../../utils/format'

const route = useRoute()
const cart = useCartStore()
const product = ref(null)
const trace = ref(null)
const showAdd = ref(false)
const number = ref(1)
const weight = ref(1)
const adding = ref(false)
const id = route.params.id

function preview() {
  if (product.value && product.value.image) {
    showImagePreview({ images: [imgUrl(product.value.image)], startPosition: 0 })
  }
}

function openAdd() {
  number.value = 1
  weight.value = 1
  showAdd.value = true
}

// 加入购物车：按份传 number，按重量传 weight（支持小数）
async function confirmAdd() {
  const payload = { productId: Number(id) }
  if (product.value.priceType === 2) {
    const w = Number(weight.value)
    if (!w || w <= 0) {
      showToast('请输入正确的重量')
      return
    }
    payload.weight = w
  } else {
    payload.number = number.value
  }
  adding.value = true
  try {
    await addCart(payload)
    showToast('已加入购物车')
    showAdd.value = false
    cart.refresh()
  } finally {
    adding.value = false
  }
}

onMounted(async () => {
  product.value = await productDetail(id)
  try {
    trace.value = await productTrace(id)
  } catch (e) {
    trace.value = null
  }
})
</script>

<style scoped>
.banner {
  background: #fff;
}
.banner .van-image {
  width: 100%;
  aspect-ratio: 1;
  height: auto;
}
.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f3f5;
  color: #c8c9cc;
}
.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.big {
  font-size: 22px;
}
.unit {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  font-style: normal;
}
.name {
  font-size: 16px;
  font-weight: 600;
  margin-top: 6px;
}
.tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}
.sales {
  font-size: 12px;
  color: #999;
}
.meta {
  margin-top: 8px;
}
.desc {
  margin-top: 8px;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}
.sec-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}
.report {
  margin-top: 8px;
}
.report-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.add-panel {
  padding: 16px;
}
.add-head {
  display: flex;
  gap: 10px;
  align-items: center;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  font-size: 14px;
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
.confirm {
  margin-top: 8px;
}
</style>
