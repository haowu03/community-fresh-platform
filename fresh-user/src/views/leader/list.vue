<template>
  <div class="page">
    <NavBar title="我发起的团购" />
    <div v-for="g in list" :key="g.id" class="card">
      <div class="main">
        <van-image :src="imgUrl(g.productImage)" width="72" height="72" radius="8" fit="cover" />
        <div class="info">
          <div class="name">{{ g.productName }}</div>
          <div class="sub">团购价 ¥{{ g.groupPrice }} · {{ g.minNum }}人成团</div>
          <div class="sub">截止 {{ fmtTime(g.endTime) }}</div>
        </div>
        <van-tag :color="groupStatusMap[g.status] && groupStatusMap[g.status].color">{{ g.statusDesc }}</van-tag>
      </div>
      <van-progress :percentage="g.progress || 0" stroke-width="6" color="#39b57f" class="progress" />
      <div class="ops">
        <span class="joined">已团 {{ g.joinedNum }}/{{ g.minNum }} 人</span>
        <van-button size="small" plain type="primary" @click="showMembers(g)">参团成员</van-button>
      </div>
    </div>
    <van-empty v-if="!list.length" description="还没有发起过团购" />

    <!-- 参团成员弹层 -->
    <van-popup v-model:show="memberShow" position="bottom" round :style="{ maxHeight: '70%' }">
      <div class="member-panel">
        <div class="sec-title">参团成员（{{ members.length }}）</div>
        <van-empty v-if="!members.length" description="暂无成员" image-size="60" />
        <van-cell v-for="m in members" :key="m.id" :title="`${m.userName}　${m.phone || ''}`"
          :label="`参团时间 ${fmtTime(m.createTime)}`" :value="`¥${m.orderAmount}`">
          <template #right-icon>
            <van-tag :type="m.status === 1 ? 'success' : 'danger'">{{ m.status === 1 ? '已支付' : '已退款' }}</van-tag>
          </template>
        </van-cell>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { myLaunch, groupMembers } from '../../api/groupbuy'
import { imgUrl, fmtTime, groupStatusMap } from '../../utils/format'

const list = ref([])
const memberShow = ref(false)
const members = ref([])

// 查看参团成员
async function showMembers(g) {
  members.value = (await groupMembers(g.id)) || []
  memberShow.value = true
}

onMounted(async () => {
  list.value = (await myLaunch()) || []
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
.ops {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.joined {
  font-size: 12px;
  color: #666;
}
.member-panel {
  padding: 16px;
  overflow-y: auto;
}
.sec-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}
</style>
