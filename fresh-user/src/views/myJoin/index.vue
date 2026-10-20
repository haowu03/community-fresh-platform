<template>
  <div class="page">
    <NavBar title="我的参团" />
    <div v-for="g in list" :key="g.id" class="card" @click="$router.push(`/groupDetail/${g.id}`)">
      <div class="main">
        <van-image :src="imgUrl(g.productImage)" width="72" height="72" radius="8" fit="cover" />
        <div class="info">
          <div class="name">{{ g.productName }}</div>
          <div class="sub">团长：{{ g.leaderName }} · 自提点：{{ g.pointName }}</div>
          <div class="sub">¥{{ g.groupPrice }} · 已团{{ g.joinedNum }}/{{ g.minNum }}人</div>
        </div>
        <van-tag :color="groupStatusMap[g.status] && groupStatusMap[g.status].color">{{ g.statusDesc }}</van-tag>
      </div>
      <van-progress :percentage="g.progress || 0" stroke-width="6" color="#39b57f" class="progress" />
    </div>
    <van-empty v-if="!list.length" description="还没有参过团" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { myJoin } from '../../api/groupbuy'
import { imgUrl, groupStatusMap } from '../../utils/format'

const list = ref([])

onMounted(async () => {
  list.value = (await myJoin()) || []
})
</script>

<style scoped>
.main {
  display: flex;
  gap: 10px;
  align-items: center;
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 15px;
  font-weight: 600;
}
.sub {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.progress {
  margin-top: 10px;
}
</style>
