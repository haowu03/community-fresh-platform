<template>
  <div class="page">
    <NavBar title="消息通知" />
    <div
      v-for="n in list"
      :key="n.id"
      class="card notice"
      :class="{ unread: n.isRead === 0 }"
      @click="onRead(n)"
    >
      <div class="head">
        <van-badge v-if="n.isRead === 0" dot color="#ee0a24">
          <van-tag plain type="primary">{{ noticeTypeText(n.type) }}</van-tag>
        </van-badge>
        <van-tag v-else plain>{{ noticeTypeText(n.type) }}</van-tag>
        <span class="title">{{ n.title }}</span>
        <span class="time">{{ fmtTime(n.createTime) }}</span>
      </div>
      <div class="content">{{ n.content }}</div>
    </div>
    <van-empty v-if="!list.length" description="暂无消息" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import NavBar from '../../components/NavBar.vue'
import { noticeList, readNotice } from '../../api/notice'
import { fmtTime, noticeTypeText } from '../../utils/format'

const list = ref([])

// 点击标记已读
async function onRead(n) {
  if (n.isRead === 0) {
    await readNotice(n.id)
    n.isRead = 1
  }
}

onMounted(async () => {
  list.value = (await noticeList()) || []
  // 进入通知页即全部标记已读，返回"我的"后红点消失
  const unreadList = list.value.filter(n => n.isRead === 0)
  if (unreadList.length) {
    await Promise.all(unreadList.map(n => readNotice(n.id).catch(() => {})))
    unreadList.forEach(n => { n.isRead = 1 })
  }
})
</script>

<style scoped>
.notice .head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.title {
  font-size: 14px;
  font-weight: 500;
}
.unread .title {
  font-weight: 700;
}
.time {
  margin-left: auto;
  font-size: 11px;
  color: #bbb;
}
.content {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
}
</style>
