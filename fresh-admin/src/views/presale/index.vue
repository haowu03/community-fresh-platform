<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openPublish">发布预售</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预售活动表格 -->
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
        <el-table-column label="预售价" width="100">
          <template #default="{ row }">¥ {{ row.presalePrice }}</template>
        </el-table-column>
        <el-table-column prop="stockLimit" label="限购库存" width="90" />
        <el-table-column prop="soldCount" label="已售" width="80" />
        <el-table-column prop="remainStock" label="剩余" width="80" />
        <el-table-column prop="endTime" label="截止时间" width="170" />
        <el-table-column prop="deliveryDate" label="发货日期" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="{ 0: 'info', 1: 'success', 2: 'danger' }[row.status]" size="small">
              {{ row.statusDesc || { 0: '未开始', 1: '进行中', 2: '已结束' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 2" link type="danger" @click="onClose(row)">提前结束</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 发布预售弹窗 -->
    <el-dialog v-model="publishVisible" title="发布预售活动" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="预售商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预售价" prop="presalePrice">
          <el-input-number v-model="form.presalePrice" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="限购库存" prop="stockLimit">
          <el-input-number v-model="form.stockLimit" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="活动开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="下单截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发货日期" prop="deliveryDate">
          <el-date-picker
            v-model="form.deliveryDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="统一发货日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onPublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import Pagination from '../../components/Pagination.vue'
import { getPresalePage, publishPresale, closePresale } from '../../api/presale'
import { getProductPage } from '../../api/product'
import { imgUrl } from '../../utils'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, status: null })

// 加载预售分页
async function loadList() {
  loading.value = true
  try {
    const res = await getPresalePage(query)
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

// ========== 发布预售 ==========
const publishVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const productOptions = ref([])
const form = reactive({
  productId: null,
  presalePrice: null,
  stockLimit: null,
  startTime: '',
  endTime: '',
  deliveryDate: ''
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  presalePrice: [{ required: true, message: '请输入预售价', trigger: 'blur' }],
  stockLimit: [{ required: true, message: '请输入限购库存', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  deliveryDate: [{ required: true, message: '请选择发货日期', trigger: 'change' }]
}

// 打开发布弹窗：加载商品下拉
async function openPublish() {
  Object.assign(form, {
    productId: null,
    presalePrice: null,
    stockLimit: null,
    startTime: '',
    endTime: '',
    deliveryDate: ''
  })
  publishVisible.value = true
  const res = await getProductPage({ page: 1, pageSize: 200, status: 1 })
  productOptions.value = res.data.records || []
}

// 提交发布
async function onPublish() {
  await formRef.value.validate()
  saving.value = true
  try {
    await publishPresale(form)
    ElMessage.success('发布成功')
    publishVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

// 提前结束
async function onClose(row) {
  await ElMessageBox.confirm(`确定提前结束商品「${row.productName}」的预售吗？`, '提示', { type: 'warning' })
  await closePresale(row.id)
  ElMessage.success('已结束')
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
