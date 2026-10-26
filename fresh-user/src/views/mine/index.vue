<template>
  <div class="page mine">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <!-- 头像：点击上传更换 -->
      <van-uploader :after-read="onAvatarChange" accept="image/*" max-count="1">
        <div class="avatar-wrap">
          <van-image v-if="user.userInfo && user.userInfo.avatar" :src="imgUrl(user.userInfo.avatar)" width="64" height="64" round fit="cover" />
          <van-icon v-else name="manager" size="52" />
          <span class="avatar-edit"><van-icon name="photograph" size="12" /></span>
        </div>
      </van-uploader>
      <div class="info">
        <div class="name" @click="openNameEdit">
          {{ displayName }}
          <van-tag v-if="user.isLeader" type="warning">团长</van-tag>
          <van-icon name="edit" size="14" class="name-edit" />
        </div>
        <div class="username">账号：{{ user.userInfo && user.userInfo.username }}</div>
      </div>
    </div>

    <!-- 功能宫格 -->
    <div class="grid-card">
      <div class="grid-item" @click="$router.push('/order')">
        <van-icon name="orders-o" size="24" class="gi-1" />
        <span>我的订单</span>
      </div>
      <div class="grid-item" @click="$router.push('/address')">
        <van-icon name="location-o" size="24" class="gi-2" />
        <span>地址管理</span>
      </div>
      <div class="grid-item" @click="$router.push('/myPosts')">
        <van-icon name="edit" size="24" class="gi-3" />
        <span>我的帖子</span>
      </div>
      <div class="grid-item" @click="$router.push('/myJoin')">
        <van-icon name="friends-o" size="24" class="gi-4" />
        <span>我的参团</span>
      </div>
    </div>

    <!-- 消息通知 -->
    <van-cell-group inset class="group">
      <van-cell title="消息通知" icon="bell" is-link to="/notice">
        <template #right-icon>
          <van-badge v-if="unread" dot color="#ee0a24" class="badge" />
          <van-icon name="arrow" color="#969799" />
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 团长专区入口（仅团长可见） -->
    <van-cell-group v-if="user.isLeader" inset title="团长专区" class="group">
      <van-cell title="发起团购" icon="add-o" is-link to="/leader/launch" />
      <van-cell title="我发起的团购" icon="cluster-o" is-link to="/leader/list" />
      <van-cell title="自提核销" icon="certificate" is-link to="/leader/verify" />
    </van-cell-group>

    <div class="logout">
      <van-button round block plain type="danger" @click="onLogout">退出登录</van-button>
    </div>

    <!-- 修改昵称弹窗 -->
    <van-dialog
      v-model:show="showNameEdit"
      title="修改昵称"
      show-cancel-button
      :before-close="onNameSave"
    >
      <van-field v-model="newName" placeholder="请输入昵称" maxlength="20" class="name-field" />
    </van-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useUserStore } from '../../stores/user'
import { noticeList } from '../../api/notice'
import { updateUserInfo, uploadFile } from '../../api/user'
import { imgUrl } from '../../utils/format'

const router = useRouter()
const user = useUserStore()
const unread = ref(0)
const showNameEdit = ref(false)
const newName = ref('')

// 昵称默认为账户名
const displayName = computed(() => {
  const u = user.userInfo
  if (!u) return '未登录'
  return u.name || u.username
})

// 上传头像 → 更新资料 → 刷新用户信息
async function onAvatarChange(file) {
  try {
    const url = await uploadFile(file.file)
    await updateUserInfo({ avatar: url })
    await user.fetchInfo()
    showToast('头像已更新')
  } catch (e) {}
}

function openNameEdit() {
  newName.value = (user.userInfo && user.userInfo.name) || ''
  showNameEdit.value = true
}

// 弹窗关闭前保存（返回 false 阻止关闭）
async function onNameSave(action) {
  if (action !== 'confirm') return true
  const name = newName.value.trim()
  if (!name) {
    showToast('昵称不能为空')
    return false
  }
  try {
    await updateUserInfo({ name })
    await user.fetchInfo()
    showToast('昵称已修改')
    return true
  } catch (e) {
    return false
  }
}

async function onLogout() {
  await showConfirmDialog({ title: '提示', message: '确定退出登录吗？' })
  user.logout()
  router.replace('/login')
}

onMounted(async () => {
  // 刷新用户信息 + 未读通知数
  user.fetchInfo().catch(() => {})
  try {
    const list = (await noticeList()) || []
    unread.value = list.filter(n => n.isRead === 0).length
  } catch (e) {}
})
</script>

<style scoped>
/* ---------- 用户卡：手账信息页 ---------- */
.user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 14px 12px 0;
  padding: 20px 16px 26px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(30, 77, 53, 0.16);
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}
.user-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: linear-gradient(180deg, var(--green-ink, #1e4d35), var(--green-sprout, #8fae92));
}
.avatar-wrap {
  position: relative;
  width: 64px;
  height: 64px;
  background: var(--paper-deep, #f0e9db);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--green-moss, #4c7b58);
}
.user-card :deep(.van-image) {
  border: 2px solid rgba(30, 77, 53, 0.3);
  border-radius: 50%;
}
.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--green-ink, #1e4d35);
  color: var(--paper, #f7f3ea);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 19px;
  font-weight: 700;
  font-family: var(--font-kai, serif);
  letter-spacing: 0.08em;
  color: var(--green-ink, #1e4d35);
  display: flex;
  align-items: center;
  gap: 8px;
}
.name-edit {
  opacity: 0.8;
}
.username {
  font-size: 12px;
  color: var(--ink-soft, #6f7267);
  margin-top: 5px;
  letter-spacing: 0.05em;
}

/* ---------- 功能宫格：票据卡 ---------- */
.grid-card {
  display: flex;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(30, 77, 53, 0.16);
  border-radius: 12px;
  margin: 12px 12px 0;
  padding: 14px 0;
  position: relative;
  z-index: 1;
}
.grid-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--ink, #2b2c26);
  position: relative;
}
.grid-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 6px;
  bottom: 6px;
  width: 1px;
  border-right: 1px dashed rgba(30, 77, 53, 0.2);
}
.grid-item:active {
  opacity: 0.6;
}
.gi-1 {
  color: var(--green-ink, #1e4d35);
}
.gi-2 {
  color: var(--yellow-ochre, #e9a13b);
}
.gi-3 {
  color: var(--green-moss, #4c7b58);
}
.gi-4 {
  color: var(--tomato, #d8402b);
}

.group {
  margin-top: 12px;
}
:deep(.van-cell-group) {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(30, 77, 53, 0.16);
  border-radius: 12px;
  overflow: hidden;
}
:deep(.van-cell-group__title) {
  font-family: var(--font-kai, serif);
  color: var(--green-ink, #1e4d35);
  letter-spacing: 0.15em;
  font-weight: 700;
}
:deep(.van-cell) {
  background: transparent;
  align-items: center;
}
:deep(.van-cell__title) {
  letter-spacing: 0.06em;
}
.badge {
  margin-right: 6px;
}
.logout {
  padding: 26px 16px;
}
.logout :deep(.van-button) {
  letter-spacing: 0.3em;
  text-indent: 0.3em;
  font-family: var(--font-kai, serif);
}
.name-field {
  margin: 12px 16px;
  width: auto;
  background: var(--paper-deep, #f0e9db);
  border-radius: 8px;
}
</style>
