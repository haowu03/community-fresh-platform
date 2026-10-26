<template>
  <div class="page category-page">
    <van-nav-bar title="商品分类" fixed placeholder />
    <div class="body">
      <!-- 左侧分类栏：独立滚动区，始终固定可见 -->
      <van-sidebar v-model="active" class="side" @change="reset">
        <van-sidebar-item v-for="c in categories" :key="c.id" :title="c.name" />
      </van-sidebar>

      <!-- 右侧商品列表：独立滚动 -->
      <div class="content">
        <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
          <div class="prod-grid">
            <ProductCard v-for="p in list" :key="p.id" :product="p" @click="$router.push(`/detail/${p.id}`)" />
          </div>
          <van-empty v-if="finished && !list.length" description="该分类暂无商品" />
        </van-list>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { categoryList, productList } from '../../api/product'
import ProductCard from '../../components/ProductCard.vue'

const categories = ref([])
const active = ref(0)
const list = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const PAGE_SIZE = 10

async function loadData() {
  const cat = categories.value[active.value]
  if (!cat) {
    loading.value = false
    finished.value = true
    return
  }
  try {
    const data = await productList({ categoryId: cat.id, page: page.value, pageSize: PAGE_SIZE })
    const records = (data && data.records) || []
    // 按 id 去重兜底：防止 van-list 自动触发与手动加载竞态导致重复渲染
    const existIds = new Set(list.value.map(p => p.id))
    records.forEach(r => {
      if (!existIds.has(r.id)) {
        list.value.push(r)
        existIds.add(r.id)
      }
    })
    page.value += 1
    if (records.length < PAGE_SIZE) finished.value = true
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 切换分类：仅重置状态，由 van-list 检测到 loading=false/finished=false 后自行触发 @load，
// 避免手动调用与组件自动触发的双重加载（曾导致商品重复显示）
function reset() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = false
}

onMounted(async () => {
  categories.value = (await categoryList()) || []
  // 分类到达后重置一次状态，让 van-list 自动发起首次加载
  if (categories.value.length && !list.value.length) {
    reset()
  }
})
</script>

<style scoped>
/* 左右两栏独立滚动的固定布局 */
.category-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.body {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.side {
  width: 92px;
  flex-shrink: 0;
  height: 100%;
  overflow-y: auto;
  background: rgba(240, 233, 219, 0.55);
}
.side::-webkit-scrollbar {
  display: none;
}
.content {
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  padding-bottom: 64px;
}
.prod-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 10px;
}
/* 宽屏自适应列数 */
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
</style>