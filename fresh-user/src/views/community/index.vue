<template>
  <div class="page">
    <van-nav-bar title="社区" fixed placeholder>
      <template #right>
        <van-icon name="edit" size="20" @click="$router.push('/postPublish')" />
      </template>
    </van-nav-bar>

    <van-tabs v-model:active="active" color="#39b57f" @change="reset" sticky offset-top="46">
      <van-tab title="团购分享" />
      <van-tab title="好物分享" />
    </van-tabs>

    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadData">
      <div v-for="p in list" :key="p.id" class="card post-card" @click="$router.push(`/postDetail/${p.id}`)">
        <div class="author">
          <van-image :src="p.avatar ? imgUrl(p.avatar) : ''" width="28" height="28" round fit="cover">
            <template #error><van-icon name="manager" size="28" color="#bbb" /></template>
            <template #loading><van-icon name="manager" size="28" color="#bbb" /></template>
          </van-image>
          <span class="nickname">{{ p.userName }}</span>
          <span class="time">{{ fmtTime(p.createTime) }}</span>
        </div>
        <div class="title">{{ p.title }}</div>
        <div class="content ellipsis-2">{{ p.content }}</div>
        <van-image v-if="firstImage(p)" :src="imgUrl(firstImage(p))" height="140" fit="cover" radius="6" class="cover" />
        <div class="foot">
          <span><van-icon name="good-job-o" :color="p.liked ? '#ee0a24' : '#999'" /> {{ p.likeCount || 0 }}</span>
          <span><van-icon name="comment-o" color="#999" /> {{ p.commentCount || 0 }}</span>
        </div>
      </div>
      <van-empty v-if="finished && !list.length" description="暂无帖子，来发第一条吧" />
    </van-list>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { postPage } from '../../api/post'
import { imgUrl, parseImages, fmtTime } from '../../utils/format'

const active = ref(0)
const list = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const PAGE_SIZE = 10

function firstImage(p) {
  return parseImages(p.images)[0] || ''
}

async function loadData() {
  try {
    const data = await postPage({ type: active.value + 1, page: page.value, pageSize: PAGE_SIZE })
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

// 切换类型后重置列表
function reset() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = true
  loadData()
}
</script>

<style scoped>
.post-card .author {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nickname {
  font-size: 13px;
  font-weight: 500;
}
.time {
  margin-left: auto;
  font-size: 11px;
  color: #bbb;
}
.title {
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
}
.content {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}
.cover {
  width: 100%;
  margin-top: 8px;
}
.foot {
  display: flex;
  gap: 20px;
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}
.foot .van-icon {
  vertical-align: -2px;
}
</style>
