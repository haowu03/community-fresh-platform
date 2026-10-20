<template>
  <div class="page">
    <NavBar title="社区团购">
      <template #right>
        <span class="my-join" @click="$router.push('/myJoin')">我的参团</span>
      </template>
    </NavBar>

    <div v-for="g in list" :key="g.id" class="card" @click="$router.push(`/groupDetail/${g.id}`)">
      <div class="main">
        <van-image :src="imgUrl(g.productImage)" width="96" height="96" radius="8" fit="cover" />
        <div class="info">
          <div class="name">{{ g.productName }}</div>
          <div class="leader">团长：{{ g.leaderName }} · 自提点：{{ g.pointName }}</div>
          <div class="price-row">
            <span class="price">¥{{ g.groupPrice }}</span>
            <span class="price-del">¥{{ g.originalPrice }}</span>
            <van-tag :color="groupStatusMap[g.status] && groupStatusMap[g.status].color">{{ g.statusDesc }}</van-tag>
          </div>
          <van-progress :percentage="g.progress || 0" stroke-width="6" color="#39b57f" class="progress" />
          <div class="meta">
            <span>{{ g.minNum }}人成团 · 已团{{ g.joinedNum }} · 剩{{ g.remainNum }}名额</span>
            <span>截止 {{ fmtTime(g.endTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <van-empty v-if="!list.length" description="暂无进行中的团购" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { groupBuyList } from '../../api/groupbuy'
import { imgUrl, fmtTime, groupStatusMap } from '../../utils/format'

const list = ref([])

onMounted(async () => {
  list.value = (await groupBuyList()) || []
})
</script>

<style scoped>
.my-join {
  font-size: 13px;
  color: #39b57f;
}
.main {
  display: flex;
  gap: 10px;
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 15px;
  font-weight: 600;
}
.leader {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 4px;
}
.price {
  font-size: 18px;
}
.progress {
  margin-top: 8px;
}
.meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
</style>
