<template>
  <div class="page home">
    <!-- 顶部手账标题区：大楷体标语 + 印章副标 -->
    <div class="home-header">
      <div class="stamp">今日开市</div>
      <div class="slogan-row">
        <div class="slogan kai">新鲜直达</div>
        <div class="slogan-sub">每日精选 · Farm-stand Daily</div>
      </div>
      <van-search
        v-model="query.name"
        shape="round"
        background="transparent"
        placeholder="搜索菜品名称"
        @search="reset"
        @clear="reset"
      />
    </div>

    <!-- 快捷入口：三枚便签卡 -->
    <div class="entry-row">
      <div class="entry-card e-expiry" @click="router.push('/nearExpiry')">
        <div class="entry-tape"></div>
        <div class="entry-text">
          <b class="kai">临期特价</b>
          <span>限时七折</span>
        </div>
        <van-icon name="clock-o" size="18" class="entry-ico" />
      </div>
      <div class="entry-card e-presale" @click="router.push('/presale')">
        <div class="entry-tape"></div>
        <div class="entry-text">
          <b class="kai">预售专区</b>
          <span>产地直发</span>
        </div>
        <van-icon name="send-gift-o" size="18" class="entry-ico" />
      </div>
      <div class="entry-card e-group" @click="router.push('/groupbuy')">
        <div class="entry-tape"></div>
        <div class="entry-text">
          <b class="kai">社区团购</b>
          <span>拼团更省</span>
        </div>
        <van-icon name="friends-o" size="18" class="entry-ico" />
      </div>
    </div>

    <!-- 分类导航（编辑式下划线标签） -->
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

    <!-- 商品列表（上拉加载，逐卡浮现） -->
    <van-list v-model:loading="loading" :finished="finished" finished-text="— 今日菜已上齐 —" @load="loadData">
      <div class="prod-grid">
        <div v-for="(p, i) in list" :key="p.id" class="grid-item" :style="{ '--i': i }">
          <ProductCard :product="p" @click="goDetail(p.id)" />
        </div>
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
/* ---------- 手账标题区 ---------- */
.home-header {
  position: relative;
  padding: 18px 8px 8px;
  border-bottom: 1.5px dashed rgba(30, 77, 53, 0.22);
  overflow: hidden;
}
.stamp {
  position: absolute;
  top: 12px;
  right: 12px;
  transform: rotate(8deg);
  color: var(--tomato, #d8402b);
  border: 1.5px solid var(--tomato, #d8402b);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 10px;
  font-family: var(--font-kai, serif);
  letter-spacing: 3px;
  opacity: 0.85;
}
.slogan-row {
  padding: 0 12px 4px;
}
.slogan {
  font-size: 30px;
  font-weight: 700;
  color: var(--green-ink, #1e4d35);
  letter-spacing: 0.18em;
  line-height: 1.25;
}
.slogan-sub {
  font-size: 10px;
  color: var(--green-moss, #4c7b58);
  letter-spacing: 0.24em;
  margin-top: 2px;
  text-transform: uppercase;
}
.home-header :deep(.van-search) {
  padding: 6px 6px 2px;
}
.home-header :deep(.van-search__content) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(30, 77, 53, 0.18);
}

/* ---------- 便签入口卡 ---------- */
.entry-row {
  display: flex;
  gap: 8px;
  padding: 12px 12px 4px;
}
.entry-card {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 12px 8px 10px;
  border-radius: 6px;
  border: 1px solid rgba(30, 77, 53, 0.16);
  box-shadow: 0 3px 8px rgba(43, 44, 38, 0.08);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.75);
  transition: transform 0.15s ease;
}
.entry-card:active {
  transform: translateY(2px);
}
.entry-tape {
  position: absolute;
  top: -1px;
  left: 0;
  right: 0;
  height: 4px;
}
.e-expiry .entry-tape {
  background: linear-gradient(90deg, #e9a13b, #d8402b);
}
.e-presale .entry-tape {
  background: var(--green-ink, #1e4d35);
}
.e-group .entry-tape {
  background: var(--green-moss, #4c7b58);
}
.e-expiry b,
.e-expiry .entry-ico {
  color: var(--tomato, #d8402b);
}
.e-presale b,
.e-presale .entry-ico {
  color: var(--green-ink, #1e4d35);
}
.e-group b,
.e-group .entry-ico {
  color: var(--green-moss, #4c7b58);
}
.entry-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.entry-text b {
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}
.entry-text span {
  font-size: 10px;
  color: var(--ink-soft, #6f7267);
  margin-top: 3px;
  letter-spacing: 0.1em;
  white-space: nowrap;
}
.entry-ico {
  position: absolute;
  right: 6px;
  bottom: 8px;
  opacity: 0.7;
}

/* ---------- 编辑式分类标签 ---------- */
.cat-scroll {
  display: flex;
  overflow-x: auto;
  white-space: nowrap;
  padding: 10px 12px 6px;
  gap: 4px;
  border-bottom: 1px solid rgba(30, 77, 53, 0.1);
}
.cat-scroll::-webkit-scrollbar {
  display: none;
}
.cat-chip {
  flex-shrink: 0;
  padding: 5px 12px;
  font-size: 13px;
  color: var(--ink-soft, #6f7267);
  position: relative;
}
.cat-chip.on {
  color: var(--green-ink, #1e4d35);
  font-family: var(--font-kai, serif);
  font-weight: 700;
  font-size: 14px;
}
.cat-chip.on::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 0;
  height: 2px;
  border-radius: 1px;
  background: linear-gradient(90deg, var(--tomato, #d8402b), var(--yellow-ochre, #e9a13b));
}

/* ---------- 商品网格：逐卡浮现（宽屏自适应增加列数） ---------- */
.prod-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 10px 12px 12px;
}
@media (min-width: 720px) {
  .prod-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (min-width: 880px) {
  .prod-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
.grid-item {
  animation: riseIn 0.5s cubic-bezier(0.22, 0.8, 0.36, 1) both;
  animation-delay: calc((var(--i) % 6) * 55ms);
}
:deep(.van-list__finished-text) {
  font-family: var(--font-kai, serif);
  color: var(--green-moss, #4c7b58);
  letter-spacing: 0.2em;
}
</style>