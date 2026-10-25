<template>
  <div class="page">
    <NavBar title="我的帖子" />
    <div v-for="p in list" :key="p.id" class="card" @click="onClick(p)">
      <div class="head">
        <van-tag plain type="primary">{{ postTypeText(p.type) }}</van-tag>
        <van-tag :color="postStatusMap[p.status] && postStatusMap[p.status].color">
          {{ postStatusMap[p.status] && postStatusMap[p.status].text }}
        </van-tag>
        <span class="time">{{ fmtTime(p.createTime) }}</span>
      </div>
      <div class="title">{{ p.title }}</div>
      <div class="content ellipsis-2">{{ p.content }}</div>
      <div v-if="p.status === 2 && p.auditRemark" class="reject">审核意见：{{ p.auditRemark }}</div>
      <div class="foot">
        <span><van-icon name="good-job-o" /> {{ p.likeCount || 0 }}</span>
        <span><van-icon name="comment-o" /> {{ p.commentCount || 0 }}</span>
      </div>
    </div>
    <van-empty v-if="!list.length" description="还没有发布过帖子" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { myPosts } from '../../api/post'
import { fmtTime, postTypeText, postStatusMap } from '../../utils/format'

const router = useRouter()
const list = ref([])

function onClick(p) {
  if (p.status === 1) {
    router.push(`/postDetail/${p.id}`)
  } else {
    showToast(p.status === 0 ? '帖子审核中' : '帖子未通过审核，仅本人可见')
  }
}

onMounted(async () => {
  list.value = (await myPosts()) || []
})
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 6px;
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
.reject {
  margin-top: 6px;
  font-size: 12px;
  color: #ee0a24;
  background: #fff2f0;
  border-radius: 4px;
  padding: 6px 8px;
}
.foot {
  display: flex;
  gap: 20px;
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}
</style>
