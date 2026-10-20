<template>
  <div class="page home">
    <!-- 顶部绿色渐变区：搜索框 -->
    <div class="home-header">
      <div class="slogan">新鲜直达 · 每日精选</div>
      <van-search
        v-model="query.name"
        shape="round"
        background="transparent"
        placeholder="搜索商品名称"
        @search="reset"
        @clear="reset"
      />
    </div>

    <!-- 快捷入口：渐变小卡片 -->
    <div class="entry-row">
      <div class="entry-card e-expiry" @click="router.push('/nearExpiry')">
        <van-icon name="clock-o" size="22" />
        <div class="entry-text">
          <b>临期特价</b>
          <span>限时折扣</span>
        </div>
      </div>
      <div class="entry-card e-presale" @click="router.push('/presale')">
        <van-icon name="send-gift-o" size="22" />
        <div class="entry-text">
          <b>预售专区</b>
          <span>产地直发</span>
        </div>
      </div>
      <div class="entry-card e-group" @click="router.push('/groupbuy')">
        <van-icon name="friends-o" size="22" />
        <div class="entry-text">
          <b>社区团购</b>
          <span>拼团更省</span>
        </div>
      </div>
    </div>

    <!-- 分类导航（横向滚动） -->
    <div class="cat-scroll">
      <span class="cat-chip" :class="{ on: query.categoryId === null }" @click="selectCat(null)">全部</span>
      <span
        v-for="c in categories"
        :key="c.id"
        class="cat-chip"
        :class="{ on: query.categoryId === c.id }"
        @click="selectCat(c.id)"
      >{{ c.name }}</span>
    </div>

    <!-- 商品列表（上拉加载） -->
    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <div class="prod-grid">
        <ProductCard v-for="p in list" :key="p.id" :product="p" @click="goDetail(p.id)" />
      </div>
      <van-empty v-if="finished && !list.length" description="暂无商品" />
    </van-list>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { categoryList, productList } from '../../api/product'
import ProductCard from '../../components/ProductCard.vue'

const router = useRouter()
const categories = ref([])
const list = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const query = reactive({ categoryId: null, name: '' })
const PAGE_SIZE = 10

// 分页加载商品
async function loadData() {
  try {
    const data = await productList({
      categoryId: query.categoryId || undefined,
      name: query.name || undefined,
      page: page.value,
      pageSize: PAGE_SIZE
    })
    const records = (data && data.records) || []
    list.value.push(...records)
    page.value += 1
    if (records.length < PAGE_SIZE) finished.value = true
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 搜索 / 切换分类后重置列表
function reset() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = true
  loadData()
}

function selectCat(id) {
  query.categoryId = id
  query.name = ''
  reset()
}

function goDetail(id) {
  router.push(`/detail/${id}`)
}

onMounted(async () => {
  categories.value = (await categoryList()) || []
})
</script>

<style scoped>
/* 顶部生鲜绿渐变区 */
.home-header {
  background: linear-gradient(135deg, #39b57f, #27a06c);
  padding: 14px 4px 10px;
  border-radius: 0 0 16px 16px;
}
.slogan {
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
  letter-spacing: 1px;
  padding: 0 16px 6px;
}
.home-header :deep(.van-search) {
  padding: 4px 8px;
}

/* 三个渐变入口卡片 */
.entry-row {
  display: flex;
  gap: 8px;
  padding: 10px 12px;
}
.entry-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 8px;
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.e-expiry {
  background: linear-gradient(135deg, #ff7a45, #f5222d);
}
.e-presale {
  background: linear-gradient(135deg, #5b7fff, #7a5af8);
}
.e-group {
  background: linear-gradient(135deg, #a85cf9, #ec5ca0);
}
.entry-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.entry-text b {
  font-size: 12px;
  white-space: nowrap;
}
.entry-text span {
  font-size: 10px;
  opacity: 0.85;
  margin-top: 2px;
  white-space: nowrap;
}

/* 分类胶囊导航 */
.cat-scroll {
  display: flex;
  overflow-x: auto;
  white-space: nowrap;
  padding: 4px 12px 10px;
  gap: 8px;
}
.cat-scroll::-webkit-scrollbar {
  display: none;
}
.cat-chip {
  flex-shrink: 0;
  padding: 5px 14px;
  font-size: 13px;
  color: #555;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
.cat-chip.on {
  color: #fff;
  background: linear-gradient(135deg, #39b57f, #27a06c);
  font-weight: 600;
}

.prod-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 2px 12px 10px;
}
</style>
