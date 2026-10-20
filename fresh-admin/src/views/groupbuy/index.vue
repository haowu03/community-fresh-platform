<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已成团" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已流团" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 团购活动表格 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="商品" min-width="160">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                v-if="row.productImage"
                :src="imgUrl(row.productImage)"
                style="width: 40px; height: 40px"
                fit="cover"
              />
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="团长" width="100">
          <template #default="{ row }">{{ row.leaderName || row.leaderId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="pointName" label="自提点" min-width="120" show-overflow-tooltip />
        <el-table-column label="团购价" width="100">
          <template #default="{ row }">¥ {{ row.groupPrice }}</template>
        </el-table-column>
        <el-table-column prop="minNum" label="成团人数" width="90" />
        <el-table-column prop="joinedNum" label="已参团" width="80" />
        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress ?? 0" :status="row.status === 4 ? 'exception' : undefined" />
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="截止时间" width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ row.statusDesc || statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openMembers(row)">参团成员</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="onClose(row)">截团</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 参团成员抽屉 -->
    <el-drawer v-model="memberVisible" :title="`参团成员 - ${currentActivity?.productName || ''}`" size="520px">
      <el-table v-loading="memberLoading" :data="memberList" border size="small">
        <el-table-column prop="userName" label="用户昵称" min-width="110" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="订单金额" width="100">
          <template #default="{ row }">¥ {{ row.orderAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已支付' : '已退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="参团时间" width="165" />
      </el-table>
      <el-empty v-if="!memberLoading && memberList.length === 0" description="暂无参团成员" />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Pagination from '../../components/Pagination.vue'
import { getGroupBuyPage, getGroupBuyMembers, closeGroupBuy } from '../../api/groupbuy'
import { imgUrl } from '../../utils'

// 状态：0未开始 1进行中 2已成团 3已完成 4已流团
const statusMap = { 0: '未开始', 1: '进行中', 2: '已成团', 3: '已完成', 4: '已流团' }

function statusTagType(s) {
  return { 0: 'info', 1: 'primary', 2: 'success', 3: 'success', 4: 'danger' }[s] || 'info'
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, status: null })

// 加载团购分页
async function loadList() {
  loading.value = true
  try {
    const res = await getGroupBuyPage(query)
    list.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  loadList()
}

// ========== 参团成员 ==========
const memberVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentActivity = ref(null)

async function openMembers(row) {
  currentActivity.value = row
  memberVisible.value = true
  memberLoading.value = true
  try {
    const res = await getGroupBuyMembers(row.id)
    memberList.value = res.data || []
  } finally {
    memberLoading.value = false
  }
}

// 手动截团
async function onClose(row) {
  await ElMessageBox.confirm(
    `确定对「${row.productName}」手动截团吗？已满足成团人数将成团，否则流团退款。`,
    '手动截团',
    { type: 'warning' }
  )
  await closeGroupBuy(row.id)
  ElMessage.success('截团成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.product-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
