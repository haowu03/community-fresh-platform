<template>
  <div class="product-card" @click="$emit('click')">
    <div class="img-wrap">
      <van-image :src="imgUrl(product.image)" fit="cover" lazy-load>
        <!-- 图片加载失败兜底 -->
        <template #error>
          <div class="img-fallback"><van-icon name="photo-fail" size="28" /></div>
        </template>
      </van-image>
      <span v-if="product.priceType === 2" class="weight-badge">按重计价</span>
    </div>
    <div class="info">
      <div class="name">{{ product.name }}</div>
      <div v-if="product.originPlace" class="origin">{{ product.originPlace }}</div>
      <div class="tags">
        <van-tag :color="freshType(product.freshType).color" round class="fresh-tag">
          {{ freshType(product.freshType).text }}
        </van-tag>
      </div>
      <div class="bottom">
        <!-- 价格区可被父页面覆盖（如临期特价折扣价） -->
        <slot name="price">
          <span class="price">¥{{ product.price }}<i class="unit">/{{ product.unit || '份' }}</i></span>
        </slot>
        <span class="sales">已售{{ product.saleCount || 0 }}</span>
      </div>
      <slot />
    </div>
  </div>
</template>

<script setup>
import { imgUrl, freshType } from '../utils/format'

defineProps({ product: { type: Object, required: true } })
defineEmits(['click'])
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}
.img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
}
.img-wrap .van-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
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
.weight-badge {
  position: absolute;
  left: 0;
  top: 8px;
  background: linear-gradient(90deg, #ff9a56, #ff7a29);
  color: #fff;
  font-size: 9px;
  line-height: 1;
  padding: 3px 6px;
  border-radius: 0 8px 8px 0;
}
.info {
  padding: 8px 10px 10px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.origin {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.tags {
  margin: 6px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 20px;
}
.fresh-tag {
  font-size: 10px;
  padding: 1px 8px;
  border: none;
  color: #fff;
}
.bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.price {
  font-size: 17px;
}
.unit {
  font-size: 11px;
  color: #999;
  font-weight: normal;
  font-style: normal;
}
.sales {
  font-size: 11px;
  color: #999;
}
</style>
