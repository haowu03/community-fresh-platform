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
      <span v-if="product.isPresale === 1" class="presale-stamp">预售</span>
    </div>
    <div class="info">
      <div class="name kai">{{ product.name }}</div>
      <div class="meta-row">
        <span v-if="product.originPlace" class="origin"><i class="dot"></i>{{ product.originPlace }}</span>
        <span class="fresh-tag" :class="'ft-' + product.freshType">{{ freshType(product.freshType).text }}</span>
      </div>
      <div class="bottom">
        <!-- 价格区可被父页面覆盖（如临期特价折扣价） -->
        <slot name="price">
          <span class="price">¥{{ product.price }}<i class="unit">/{{ product.unit || '份' }}</i></span>
        </slot>
        <span class="sales">已售 {{ product.saleCount || 0 }}</span>
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
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(30, 77, 53, 0.16);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(43, 44, 38, 0.07);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  position: relative;
}
.product-card:active {
  transform: scale(0.975);
  box-shadow: 0 1px 4px rgba(43, 44, 38, 0.1);
}
.img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: var(--paper-deep, #f0e9db);
}
.img-wrap :deep(.van-image) {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.img-wrap :deep(img) {
  transition: transform 0.5s ease;
}
.product-card:hover .img-wrap :deep(img) {
  transform: scale(1.05);
}
.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #efe8d8;
  color: #c9bfa8;
}
.weight-badge {
  position: absolute;
  left: 0;
  top: 7px;
  background: linear-gradient(90deg, #e9a13b, #d8402b);
  color: #fff;
  font-size: 8px;
  line-height: 1;
  padding: 2px 6px 2px 4px;
  border-radius: 0 3px 3px 0;
  letter-spacing: 1px;
}
.presale-stamp {
  position: absolute;
  right: 7px;
  top: 7px;
  transform: rotate(8deg);
  color: var(--green-ink, #1e4d35);
  border: 1.2px solid var(--green-ink, #1e4d35);
  border-radius: 3px;
  font-size: 9px;
  font-family: var(--font-kai, serif);
  padding: 1px 4px;
  background: rgba(247, 243, 234, 0.8);
  letter-spacing: 2px;
}

/* 信息区：宽屏四列下的紧凑比例 */
.info {
  padding: 7px 9px 9px;
  border-top: 1px dashed rgba(30, 77, 53, 0.2);
  position: relative;
}
/* 撕票凹口 */
.info::before,
.info::after {
  content: '';
  position: absolute;
  top: -5.5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--paper, #f7f3ea);
  border: 1px solid rgba(30, 77, 53, 0.16);
  border-top-color: transparent;
  border-left-color: transparent;
}
.info::before {
  left: -7px;
  transform: rotate(45deg);
}
.info::after {
  right: -7px;
  transform: rotate(225deg);
}
.name {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--ink, #2b2c26);
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 2px;
  gap: 6px;
}
.origin {
  font-size: 10px;
  color: var(--green-moss, #4c7b58);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.origin .dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--green-sprout, #8fae92);
  flex-shrink: 0;
}
.fresh-tag {
  flex-shrink: 0;
  font-size: 9px;
  padding: 1px 6px;
  border-radius: 3px;
  letter-spacing: 1px;
}
.ft-0 {
  color: var(--green-ink, #1e4d35);
  background: rgba(143, 174, 146, 0.22);
}
.ft-1 {
  color: #9a6a00;
  background: rgba(233, 161, 59, 0.2);
}
.ft-2 {
  color: #356a8a;
  background: rgba(94, 148, 175, 0.18);
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
  font-size: 10px;
  color: #a3a69a;
  font-weight: 400;
  font-style: normal;
  font-family: var(--font-body);
  margin-left: 1px;
}
.sales {
  font-size: 10px;
  color: #a3a69a;
  letter-spacing: 0.5px;
}

/* 手机窄屏（两列）：恢复更宽松的比例 */
@media (max-width: 719px) {
  .info {
    padding: 8px 10px 10px;
  }
  .info::before,
  .info::after {
    top: -6.5px;
    width: 12px;
    height: 12px;
  }
  .name {
    font-size: 15px;
  }
  .meta-row {
    margin: 5px 0 3px;
  }
  .origin {
    font-size: 11px;
  }
  .origin .dot {
    width: 5px;
    height: 5px;
  }
  .fresh-tag {
    font-size: 10px;
    padding: 1px 7px;
  }
  .weight-badge {
    font-size: 9px;
    top: 8px;
    padding: 3px 7px 3px 5px;
  }
  .presale-stamp {
    font-size: 10px;
    padding: 1px 5px;
    border-width: 1.5px;
    border-radius: 4px;
  }
  .price {
    font-size: 18px;
  }
  .unit {
    font-size: 11px;
  }
  .sales {
    font-size: 11px;
  }
}
</style>