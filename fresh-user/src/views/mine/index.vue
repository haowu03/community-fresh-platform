<template>
  <div class="page mine">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <!-- 头像：点击上传更换 -->
      <van-uploader :after-read="onAvatarChange" accept="image/*" max-count="1">
        <div class="avatar-wrap">
          <van-image v-if="user.userInfo && user.userInfo.avatar" :src="imgUrl(user.userInfo.avatar)" width="64" height="64" round fit="cover" />
          <van-icon v-else name="manager" size="64" color="#fff" />
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
        <van-icon name="orders-o" size="26" color="#39b57f" />
        <span>我的订单</span>
      </div>
      <div class="grid-item" @click="$router.push('/address')">
        <van-icon name="location-o" size="26" color="#ff7a45" />
        <span>地址管理</span>
      </div>
      <div class="grid-item" @click="$router.push('/myPosts')">
        <van-icon name="edit" size="26" color="#5b7fff" />
        <span>我的帖子</span>
      </div>
      <div class="grid-item" @click="$router.push('/myJoin')">
        <van-icon name="friends-o" size="26" color="#a85cf9" />
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
.user-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 32px 20px 40px;
  background: linear-gradient(135deg, #39b57f, #27a06c);
  border-radius: 0 0 20px 20px;
  color: #fff;
}
.avatar-wrap {
  position: relative;
  width: 64px;
  height: 64px;
}
.user-card :deep(.van-image),
.user-card .van-icon.manager {
  border: 2px solid rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}
.avatar-edit {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  color: #27a06c;
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
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}
.name-edit {
  opacity: 0.8;
}
.username {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}

/* 功能宫格：悬浮卡片压在渐变 header 上 */
.grid-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  margin: -24px 12px 0;
  padding: 16px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
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
  color: #333;
}
.grid-item:active {
  opacity: 0.6;
}

.group {
  margin-top: 12px;
}
.badge {
  margin-right: 6px;
}
.logout {
  padding: 24px 16px;
}
.name-field {
  margin: 12px 16px;
  width: auto;
  background: #f5f6f8;
  border-radius: 8px;
}
</style>
