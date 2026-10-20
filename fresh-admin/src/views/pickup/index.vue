<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="自提点名称">
          <el-input v-model="query.name" placeholder="请输入名称" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openEdit()">新增自提点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 自提点表格 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="自提点名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="leaderName" label="团长" width="100">
          <template #default="{ row }">{{ row.leaderName || row.leaderId || '-' }}</template>
        </el-table-column>
        <el-table-column label="地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ [row.provinceName, row.cityName, row.districtName, row.detailAddress].filter(Boolean).join('') }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="businessHours" label="营业时间" width="130" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
              @change="(val) => onStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openSlots(row)">时间段</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 新增/编辑自提点弹窗 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑自提点' : '新增自提点'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入自提点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团长ID" prop="leaderId">
              <el-input-number v-model="form.leaderId" :min="1" placeholder="团长 user.id" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="省">
              <el-input v-model="form.provinceName" placeholder="省" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="市">
              <el-input v-model="form.cityName" placeholder="市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区">
              <el-input v-model="form.districtName" placeholder="区" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="detailAddress">
              <el-input v-model="form.detailAddress" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input-number v-model="form.lng" :precision="6" :step="0.0001" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input-number v-model="form.lat" :precision="6" :step="0.0001" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间">
              <el-input v-model="form.businessHours" placeholder="如 08:00-21:00" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 时间段维护弹窗 -->
    <el-dialog v-model="slotVisible" :title="`时间段维护 - ${currentPoint?.name || ''}`" width="640px">
      <!-- 新增时间段 -->
      <el-form inline>
        <el-form-item label="开始时间">
          <el-time-picker
            v-model="slotForm.startTime"
            value-format="HH:mm:ss"
            format="HH:mm"
            placeholder="开始"
            style="width: 110px"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker
            v-model="slotForm.endTime"
            value-format="HH:mm:ss"
            format="HH:mm"
            placeholder="结束"
            style="width: 110px"
          />
        </el-form-item>
        <el-form-item label="最大订单数">
          <el-input-number v-model="slotForm.maxOrders" :min="1" style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="slotSaving" @click="onAddSlot">新增时间段</el-button>
        </el-form-item>
      </el-form>
      <!-- 时间段列表 -->
      <el-table v-loading="slotLoading" :data="slotList" border size="small">
        <el-table-column prop="startTime" label="开始时间" width="110" />
        <el-table-column prop="endTime" label="结束时间" width="110" />
        <el-table-column prop="maxOrders" label="最大订单数" width="110" />
        <el-table-column prop="usedOrders" label="已用" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button link type="danger" @click="onDeleteSlot(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import Pagination from '../../components/Pagination.vue'
import {
  getPickupPage,
  addPickupPoint,
  updatePickupPoint,
  deletePickupPoint,
  updatePickupStatus,
  getTimeSlots,
  saveTimeSlot,
  deleteTimeSlot
} from '../../api/pickup'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, name: '', status: null })

// 加载自提点分页
async function loadList() {
  loading.value = true
  try {
    const res = await getPickupPage(query)
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

// ========== 新增/编辑 ==========
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const emptyForm = () => ({
  id: null,
  name: '',
  leaderId: null,
  provinceName: '',
  cityName: '',
  districtName: '',
  detailAddress: '',
  lng: null,
  lat: null,
  phone: '',
  businessHours: ''
})
const form = reactive(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入自提点名称', trigger: 'blur' }],
  leaderId: [{ required: true, message: '请输入团长ID', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function openEdit(row) {
  Object.assign(form, emptyForm())
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name,
      leaderId: row.leaderId,
      provinceName: row.provinceName || '',
      cityName: row.cityName || '',
      districtName: row.districtName || '',
      detailAddress: row.detailAddress || '',
      lng: row.lng != null ? Number(row.lng) : null,
      lat: row.lat != null ? Number(row.lat) : null,
      phone: row.phone || '',
      businessHours: row.businessHours || ''
    })
  }
  editVisible.value = true
}

// 保存自提点
async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updatePickupPoint(form)
    } else {
      await addPickupPoint(form)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

// 启停
async function onStatusChange(row, val) {
  await updatePickupStatus(val ? 1 : 0, row.id)
  ElMessage.success(val ? '已启用' : '已停用')
  loadList()
}

// 删除
async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除自提点「${row.name}」吗？`, '提示', { type: 'warning' })
  await deletePickupPoint(row.id)
  ElMessage.success('删除成功')
  loadList()
}

// ========== 时间段维护 ==========
const slotVisible = ref(false)
const slotLoading = ref(false)
const slotSaving = ref(false)
const slotList = ref([])
const currentPoint = ref(null)
const slotForm = reactive({ startTime: '', endTime: '', maxOrders: 50 })

// 打开时间段弹窗
function openSlots(row) {
  currentPoint.value = row
  Object.assign(slotForm, { startTime: '', endTime: '', maxOrders: 50 })
  slotVisible.value = true
  loadSlots()
}

async function loadSlots() {
  slotLoading.value = true
  try {
    const res = await getTimeSlots(currentPoint.value.id)
    slotList.value = res.data || []
  } finally {
    slotLoading.value = false
  }
}

// 新增时间段
async function onAddSlot() {
  if (!slotForm.startTime || !slotForm.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  slotSaving.value = true
  try {
    await saveTimeSlot({
      pointId: currentPoint.value.id,
      startTime: slotForm.startTime,
      endTime: slotForm.endTime,
      maxOrders: slotForm.maxOrders,
      status: 1
    })
    ElMessage.success('新增成功')
    Object.assign(slotForm, { startTime: '', endTime: '', maxOrders: 50 })
    loadSlots()
  } finally {
    slotSaving.value = false
  }
}

// 删除时间段
async function onDeleteSlot(row) {
  await ElMessageBox.confirm('确定删除该时间段吗？', '提示', { type: 'warning' })
  await deleteTimeSlot(row.id)
  ElMessage.success('删除成功')
  loadSlots()
}

onMounted(loadList)
</script>
