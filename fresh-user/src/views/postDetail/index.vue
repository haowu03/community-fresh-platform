<template>
  <div class="page">
    <NavBar title="帖子详情" />
    <div v-if="post">
      <!-- 帖子正文 -->
      <div class="card">
        <div class="author">
          <van-image :src="post.avatar ? imgUrl(post.avatar) : ''" width="36" height="36" round fit="cover">
            <template #error><van-icon name="manager" size="36" color="#bbb" /></template>
            <template #loading><van-icon name="manager" size="36" color="#bbb" /></template>
          </van-image>
          <div>
            <div class="nickname">{{ post.userName }}</div>
            <div class="time">{{ fmtTime(post.createTime) }}</div>
          </div>
          <van-tag plain type="primary" class="type-tag">{{ postTypeText(post.type) }}</van-tag>
        </div>
        <div class="title">{{ post.title }}</div>
        <div class="content">{{ post.content }}</div>

        <!-- 图片九宫格 -->
        <van-grid v-if="images.length" :column-num="3" :gutter="6" :border="false" class="imgs">
          <van-grid-item v-for="(img, i) in images" :key="i">
            <van-image :src="imgUrl(img)" width="100%" height="100" fit="cover" radius="6" @click="preview(i)" />
          </van-grid-item>
        </van-grid>

        <!-- 点赞 -->
        <div class="like-row" @click="onLike">
          <van-icon :name="post.liked ? 'good-job' : 'good-job-o'" size="22" :color="post.liked ? '#ee0a24' : '#666'" />
          <span :style="{ color: post.liked ? '#ee0a24' : '#666' }">{{ post.likeCount || 0 }}</span>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="card">
        <div class="sec-title">评论（{{ post.commentCount || 0 }}）</div>
        <van-empty v-if="!comments.length" description="暂无评论" image-size="60" />
        <div v-for="c in comments" :key="c.id" class="comment">
          <van-image :src="c.avatar ? imgUrl(c.avatar) : ''" width="28" height="28" round fit="cover">
            <template #error><van-icon name="manager" size="28" color="#bbb" /></template>
            <template #loading><van-icon name="manager" size="28" color="#bbb" /></template>
          </van-image>
          <div class="c-body">
            <div class="c-head">
              <span class="c-name">{{ c.userName }}</span>
              <span class="c-time">{{ fmtTime(c.createTime) }}</span>
            </div>
            <div class="c-content">{{ c.content }}</div>
            <span class="reply" @click="replyTo(c)">回复</span>
            <!-- 子评论 -->
            <div v-for="child in c.children || []" :key="child.id" class="child">
              <span class="c-name">{{ child.userName }}：</span>
              <span>{{ child.content }}</span>
              <span class="reply" @click="replyTo(child)">回复</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bottom-placeholder" />

      <!-- 底部评论输入框 -->
      <div class="comment-bar">
        <van-field
          v-model="content"
          :placeholder="replyTarget ? `回复 ${replyTarget.userName}：` : '说点什么...'"
          class="input"
        >
          <template #button>
            <van-button size="small" type="success" :disabled="!content.trim()" @click="onComment">发送</van-button>
          </template>
        </van-field>
        <van-tag v-if="replyTarget" closeable type="primary" class="reply-tag" @close="replyTarget = null">
          回复 {{ replyTarget.userName }}
        </van-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showImagePreview } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { postDetail, commentPost, likePost } from '../../api/post'
import { imgUrl, parseImages, fmtTime, postTypeText } from '../../utils/format'

const route = useRoute()
const id = route.params.id
const post = ref(null)
const comments = ref([])
const content = ref('')
const replyTarget = ref(null)

const images = computed(() => parseImages(post.value && post.value.images))

function preview(i) {
  showImagePreview({ images: images.value.map(imgUrl), startPosition: i })
}

async function load() {
  const data = await postDetail(id)
  post.value = data.post
  comments.value = data.comments || []
}

// 点赞切换
async function onLike() {
  await likePost(id)
  post.value.liked = !post.value.liked
  post.value.likeCount = (post.value.likeCount || 0) + (post.value.liked ? 1 : -1)
}

function replyTo(c) {
  replyTarget.value = c
}

// 发表评论（支持楼中楼 parentId）
async function onComment() {
  await commentPost({ postId: Number(id), content: content.value.trim(), parentId: replyTarget.value ? replyTarget.value.id : null })
  content.value = ''
  replyTarget.value = null
  load()
}

onMounted(load)
</script>

<style scoped>
.author {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nickname {
  font-size: 14px;
  font-weight: 500;
}
.time {
  font-size: 11px;
  color: #bbb;
}
.type-tag {
  margin-left: auto;
}
.title {
  font-size: 17px;
  font-weight: 700;
  margin-top: 10px;
}
.content {
  font-size: 14px;
  color: #333;
  line-height: 1.7;
  margin-top: 8px;
  white-space: pre-wrap;
}
.imgs {
  margin-top: 10px;
}
.like-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 14px;
  font-size: 14px;
}
.sec-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}
.comment {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.c-body {
  flex: 1;
  min-width: 0;
}
.c-head {
  display: flex;
  justify-content: space-between;
}
.c-name {
  font-size: 13px;
  font-weight: 500;
  color: #555;
}
.c-time {
  font-size: 11px;
  color: #bbb;
}
.c-content {
  font-size: 13px;
  margin-top: 4px;
}
.reply {
  font-size: 12px;
  color: #1989fa;
  margin-left: 8px;
}
.child {
  margin-top: 6px;
  background: #f7f8fa;
  border-radius: 6px;
  padding: 6px 8px;
  font-size: 12px;
  color: #555;
}
.comment-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 6px 8px;
}
.comment-bar .input {
  flex: 1;
  background: #f5f6f8;
  border-radius: 18px;
  padding: 6px 12px;
}
.reply-tag {
  margin-left: 6px;
}
</style>
