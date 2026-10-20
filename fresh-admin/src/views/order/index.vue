<template>
  <div class="page-container">
    <!-- 订单统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card" @click="filterStatus(2)">
          <div class="stat-label">待接单</div>
          <div class="stat-value danger">{{ statistics.toBeConfirmed ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card" @click="filterStatus(3)">
          <div class="stat-label">备货中</div>
          <div class="stat-value warn">{{ statistics.confirmed ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card" @click="filterStatus(4)">
          <div class="stat-label">配送中 / 待自提</div>
          <div class="stat-value primary">{{ statistics.deliveryInProgress ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="订单号">
          <el-input v-model="query.number" placeholder="请输入订单号" clearable style="width: 180px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="请输入手机号" clearable style="width: 150px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option v-for="(txt, s) in statusMap" :key="s" :label="txt" :value="Number(s)" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="success" @click="verifyVisible = true">自提码核销</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单表格 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="number" label="订单号" min-width="170" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="{ 1: '', 2: 'warning', 3: 'success' }[row.orderType]" size="small">
              {{ { 1: '普通', 2: '预售', 3: '团购' }[row.orderType] || '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="履约方式" width="90">
          <template #default="{ row }">
            <el-tag :type="row.deliveryType === 2 ? 'success' : 'primary'" size="small" effect="plain">
              {{ row.deliveryType === 2 ? '自提' : '配送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥ {{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="170" />
        <el-table-column label="操作" min-width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 2">
              <el-button link type="success" @click="onConfirm(row)">接单</el-button>
              <el-button link type="danger" @click="openReason(row, 'reject')">拒单</el-button>
            </template>
            <el-button v-if="row.status === 3" link type="warning" @click="onDelivery(row)">
              {{ row.deliveryType === 2 ? '备货完成' : '派送' }}
            </el-button>
            <el-button v-if="row.status === 4" link type="success" @click="onComplete(row)">完成</el-button>
            <el-button v-if="[2, 3].includes(row.status)" link type="danger" @click="openReason(row, 'cancel')">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="560px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号" :span="2">{{ detail.number }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ statusMap[detail.status] }}</el-descriptions-item>
          <el-descriptions-item label="订单类型">
            {{ { 1: '普通', 2: '预售', 3: '团购' }[detail.orderType] || '普通' }}
          </el-descriptions-item>
          <el-descriptions-item label="履约方式">{{ detail.deliveryType === 2 ? '自提' : '配送' }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥ {{ detail.amount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ detail.orderTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="2">
            {{ detail.cancelReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectionReason" label="拒单原因" :span="2">
            {{ detail.rejectionReason }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 配送收货信息 -->
        <template v-if="detail.deliveryType !== 2">
          <h4 class="drawer-subtitle">收货信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="收货人">{{ detail.consignee || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ detail.address || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <!-- 自提信息 -->
        <template v-else>
          <h4 class="drawer-subtitle">自提信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="自提点">{{ detail.pickupPointName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="自提时间段">{{ detail.timeSlotDesc || '-' }}</el-descriptions-item>
            <el-descriptions-item label="自提码">
              <span class="pickup-code">{{ detail.pickupCode || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.userName || '-' }} {{ detail.phone || '' }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <!-- 订单明细 -->
        <h4 class="drawer-subtitle">商品明细</h4>
        <el-table :data="detail.orderDetailList || []" border size="small">
          <el-table-column label="图片" width="70">
            <template #default="{ row }">
              <el-image
                v-if="row.image"
                :src="imgUrl(row.image)"
                style="width: 40px; height: 40px"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品" min-width="120" show-overflow-tooltip />
          <el-table-column label="数量" width="110">
            <template #default="{ row }">
              <span v-if="row.number">× {{ row.number }}</span>
              <span v-else-if="row.weight">{{ row.weight }}（称重）</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">¥ {{ row.amount }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <!-- 拒单/取消原因弹窗 -->
    <el-dialog v-model="reasonVisible" :title="reasonType === 'reject' ? '拒单原因' : '取消原因'" width="420px">
      <el-input v-model="reasonText" type="textarea" :rows="3" placeholder="请输入原因（必填）" />
      <template #footer>
        <el-button @click="reasonVisible = false">取消</el-button>
        <el-button type="primary" :loading="reasonSaving" @click="onReasonSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 自提码核销弹窗 -->
    <el-dialog v-model="verifyVisible" title="自提码核销" width="420px">
      <el-input
        v-model="verifyCode"
        placeholder="请输入 6 位自提码"
        maxlength="6"
        clearable
        @keyup.enter="onVerify"
      />
      <template #footer>
        <el-button @click="verifyVisible = false">取消</el-button>
        <el-button type="primary" :loading="verifying" @click="onVerify">核销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Pagination from '../../components/Pagination.vue'
import {
  searchOrders,
  getOrderDetail,
  getOrderStatistics,
  confirmOrder,
  rejectOrder,
  deliveryOrder,
  completeOrder,
  cancelOrder,
  verifyPickupCode
} from '../../api/order'
import { imgUrl } from '../../utils'

// 订单状态：1待付款 2待接单 3备货中 4配送中/待自提 5已完成 6已取消
const statusMap = {
  1: '待付款',
  2: '待接单',
  3: '备货中',
  4: '配送中/待自提',
  5: '已完成',
  6: '已取消'
}

function statusTagType(s) {
  return { 1: 'info', 2: 'danger', 3: 'warning', 4: 'primary', 5: 'success', 6: 'info' }[s] || 'info'
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const statistics = ref({})
const query = reactive({ page: 1, pageSize: 10, number: '', phone: '', status: null })
const timeRange = ref(null)

// 加载订单分页
async function loadList() {
  loading.value = true
  try {
    const params = { ...query }
    if (timeRange.value && timeRange.value.length === 2) {
      params.beginTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }
    const res = await searchOrders(params)
    list.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

// 加载统计卡片
async function loadStatistics() {
  const res = await getOrderStatistics()
  statistics.value = res.data || {}
}

function onSearch() {
  query.page = 1
  loadList()
}

// 重置搜索条件
function onReset() {
  query.number = ''
  query.phone = ''
  query.status = null
  timeRange.value = null
  onSearch()
}

// 点击统计卡片按状态过滤
function filterStatus(s) {
  query.status = s
  onSearch()
}

// 刷新：列表 + 统计
function refresh() {
  loadList()
  loadStatistics()
}

// 接单
async function onConfirm(row) {
  await ElMessageBox.confirm(`确定接单订单「${row.number}」吗？`, '接单', { type: 'info' })
  await confirmOrder(row.id)
  ElMessage.success('接单成功')
  refresh()
}

// 派送 / 备货完成
async function onDelivery(row) {
  const tip = row.deliveryType === 2 ? '备货完成后将生成自提码并通知用户，确定吗？' : '确定开始派送该订单吗？'
  await ElMessageBox.confirm(tip, '提示', { type: 'info' })
  await deliveryOrder(row.id)
  ElMessage.success('操作成功')
  refresh()
}

// 完成订单
async function onComplete(row) {
  await ElMessageBox.confirm('确定完成该订单吗？', '提示', { type: 'info' })
  await completeOrder(row.id)
  ElMessage.success('订单已完成')
  refresh()
}

// ========== 拒单/取消 ==========
const reasonVisible = ref(false)
const reasonSaving = ref(false)
const reasonType = ref('reject') // reject=拒单 cancel=取消
const reasonText = ref('')
const reasonOrder = ref(null)

function openReason(row, type) {
  reasonOrder.value = row
  reasonType.value = type
  reasonText.value = ''
  reasonVisible.value = true
}

// 提交拒单/取消
async function onReasonSubmit() {
  if (!reasonText.value.trim()) {
    ElMessage.warning('请输入原因')
    return
  }
  reasonSaving.value = true
  try {
    if (reasonType.value === 'reject') {
      await rejectOrder({ id: reasonOrder.value.id, rejectionReason: reasonText.value })
      ElMessage.success('已拒单')
    } else {
      await cancelOrder({ id: reasonOrder.value.id, cancelReason: reasonText.value })
      ElMessage.success('已取消')
    }
    reasonVisible.value = false
    refresh()
  } finally {
    reasonSaving.value = false
  }
}

// ========== 订单详情 ==========
const detailVisible = ref(false)
const detail = ref(null)

async function openDetail(row) {
  const res = await getOrderDetail(row.id)
  detail.value = res.data
  detailVisible.value = true
}

// ========== 自提码核销 ==========
const verifyVisible = ref(false)
const verifying = ref(false)
const verifyCode = ref('')

async function onVerify() {
  if (!verifyCode.value.trim()) {
    ElMessage.warning('请输入自提码')
    return
  }
  verifying.value = true
  try {
    await verifyPickupCode({ pickupCode: verifyCode.value.trim() })
    ElMessage.success('核销成功')
    verifyVisible.value = false
    verifyCode.value = ''
    refresh()
  } finally {
    verifying.value = false
  }
}

onMounted(refresh)
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  cursor: pointer;
}

.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-value.warn {
  color: #e6a23c;
}

.stat-value.primary {
  color: #409eff;
}

.drawer-subtitle {
  margin: 18px 0 10px;
  font-size: 14px;
  color: #303133;
}

.pickup-code {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
  letter-spacing: 2px;
}
</style>
