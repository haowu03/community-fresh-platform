<template>
  <div class="page-container">
    <!-- 审核状态 Tab -->
    <el-card class="table-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="待审核" name="0" />
        <el-tab-pane label="已通过" name="1" />
        <el-tab-pane label="已拒绝" name="2" />
      </el-tabs>

      <!-- 帖子表格 -->
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'primary'" size="small">
              {{ row.type === 1 ? '团购分享' : '好物分享' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="作者" width="120" />
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column label="审核意见" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.auditRemark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openContent(row)">查看内容</el-button>
            <template v-if="row.status === 0">
              <el-button link type="success" @click="onAudit(row, 1)">通过</el-button>
              <el-button link type="danger" @click="onAudit(row, 2)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 帖子内容弹窗 -->
    <el-dialog v-model="contentVisible" :title="currentPost?.title || '帖子内容'" width="640px">
      <template v-if="currentPost">
        <div class="post-meta">
          <el-tag :type="currentPost.type === 1 ? 'success' : 'primary'" size="small">
            {{ currentPost.type === 1 ? '团购分享' : '好物分享' }}
          </el-tag>
          <span>作者：{{ currentPost.userName }}</span>
          <span>发布时间：{{ currentPost.createTime }}</span>
        </div>
        <div class="post-content">{{ currentPost.content }}</div>
        <!-- 帖子图片（JSON 数组字符串） -->
        <div v-if="postImages.length" class="post-images">
          <el-image
            v-for="(img, i) in postImages"
            :key="i"
            :src="imgUrl(img)"
            :preview-src-list="postImages.map(imgUrl)"
            :initial-index="i"
            preview-teleported
            style="width: 120px; height: 120px"
            fit="cover"
          />
        </div>
        <el-divider />
        <div class="post-stats">
          <span>点赞 {{ currentPost.likeCount }}</span>
          <span>评论 {{ currentPost.commentCount }}</span>
          <span v-if="currentPost.auditRemark">审核意见：{{ currentPost.auditRemark }}</span>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝意见弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="420px">
      <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见（必填）" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="onRejectSubmit">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '../../components/Pagination.vue'
import { getPostPage, auditPost } from '../../api/post'
import { imgUrl } from '../../utils'

const activeTab = ref('0')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10 })

// 加载帖子分页（status: 0待审核 1已通过 2已拒绝）
async function loadList() {
  loading.value = true
  try {
    const res = await getPostPage({ ...query, status: Number(activeTab.value) })
    list.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

// 切换 tab 重新加载
function onTabChange() {
  query.page = 1
  loadList()
}

// ========== 查看内容 ==========
const contentVisible = ref(false)
const currentPost = ref(null)

// 解析图片 JSON 数组字符串
const postImages = computed(() => {
  if (!currentPost.value || !currentPost.value.images) return []
  try {
    const arr = JSON.parse(currentPost.value.images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

function openContent(row) {
  currentPost.value = row
  contentVisible.value = true
}

// ========== 审核 ==========
const auditing = ref(false)
const rejectVisible = ref(false)
const auditRemark = ref('')
const auditTarget = ref(null)

// 审核：1 通过（直接确认）/ 2 拒绝（弹意见框）
async function onAudit(row, status) {
  if (status === 1) {
    await ElMessageBox.confirm(`确定通过帖子「${row.title}」吗？`, '审核通过', { type: 'info' })
    await auditPost({ id: row.id, status: 1 })
    ElMessage.success('已通过')
    loadList()
  } else {
    auditTarget.value = row
    auditRemark.value = ''
    rejectVisible.value = true
  }
}

// 提交拒绝
async function onRejectSubmit() {
  if (!auditRemark.value.trim()) {
    ElMessage.warning('请输入审核意见')
    return
  }
  auditing.value = true
  try {
    await auditPost({ id: auditTarget.value.id, status: 2, auditRemark: auditRemark.value })
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    loadList()
  } finally {
    auditing.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.post-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
}

.post-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.post-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 13px;
}
</style>
