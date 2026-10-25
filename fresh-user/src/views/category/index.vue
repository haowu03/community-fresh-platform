<template>
  <div class="page category-page">
    <van-nav-bar title="商品分类" fixed placeholder />
    <div class="body">
      <!-- 左侧分类栏 -->
      <van-sidebar v-model="active" class="side" @change="reset">
        <van-sidebar-item v-for="c in categories" :key="c.id" :title="c.name" />
      </van-sidebar>

      <!-- 右侧商品列表 -->
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
    list.value.push(...records)
    page.value += 1
    if (records.length < PAGE_SIZE) finished.value = true
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 切换分类重置列表
function reset() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = true
  loadData()
}

onMounted(async () => {
  categories.value = (await categoryList()) || []
  // van-list 挂载时会先于分类数据触发一次 load（此时 cat 为空被置为 finished），
  // 分类到达后需要手动触发一次加载
  if (categories.value.length && !list.value.length) {
    reset()
  }
})
</script>

<style scoped>
.body {
  display: flex;
  align-items: flex-start;
}
.side {
  width: 85px;
  flex-shrink: 0;
  min-height: calc(100vh - 46px - 50px);
}
.content {
  flex: 1;
  min-width: 0;
}
.prod-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 10px;
}
</style>
