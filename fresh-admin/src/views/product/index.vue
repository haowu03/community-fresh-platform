<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="商品名称">
          <el-input v-model="query.name" placeholder="请输入商品名称" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 140px">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="起售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="保鲜类型">
          <el-select v-model="query.freshType" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="常温" :value="0" />
            <el-option label="冷藏" :value="1" />
            <el-option label="冷冻" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openEdit()">新增商品</el-button>
          <el-button type="warning" :icon="Warning" @click="openWarnList">库存预警</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品表格 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="imgUrl(row.image)"
              :preview-src-list="[imgUrl(row.image)]"
              preview-teleported
              style="width: 50px; height: 50px"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥ {{ row.price }}</template>
        </el-table-column>
        <el-table-column label="计价方式" width="110">
          <template #default="{ row }">
            {{ row.priceType === 2 ? `按重量/${row.unit || ''}` : '按份' }}
          </template>
        </el-table-column>
        <el-table-column label="保鲜类型" width="90">
          <template #default="{ row }">
            <el-tag :type="freshTagType(row.freshType)" size="small">{{ freshText(row.freshType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="90">
          <template #default="{ row }">
            <span :class="{ 'stock-warn': row.warnStock != null && row.stock < row.warnStock }">
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              inline-prompt
              active-text="起售"
              inactive-text="停售"
              @change="(val) => onStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openBatch(row)">批次</el-button>
            <el-button link type="success" @click="openTrace(row)">溯源</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 新增/编辑商品弹窗 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑商品' : '新增商品'" width="720px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计价方式" prop="priceType">
              <el-radio-group v-model="form.priceType">
                <el-radio :value="1">按份</el-radio>
                <el-radio :value="2">按重量</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.priceType === 2">
            <el-form-item label="计价单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择单位" style="width: 100%">
                <el-option label="斤" value="斤" />
                <el-option label="kg" value="kg" />
                <el-option label="g" value="g" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保鲜类型" prop="freshType">
              <el-radio-group v-model="form.freshType">
                <el-radio :value="0">常温</el-radio>
                <el-radio :value="1">冷藏</el-radio>
                <el-radio :value="2">冷冻</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保质期(天)">
              <el-input-number v-model="form.shelfLifeDays" :min="1" :max="3650" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警库存">
              <el-input-number v-model="form.warnStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地">
              <el-input v-model="form.originPlace" placeholder="请输入产地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品图片">
              <ImageUpload v-model="form.image" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="商品描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批次管理弹窗 -->
    <el-dialog v-model="batchVisible" :title="`批次管理 - ${currentProduct?.name || ''}`" width="860px" top="5vh">
      <!-- 批次入库表单 -->
      <el-form inline class="batch-in-form">
        <el-form-item label="生产日期">
          <el-date-picker
            v-model="batchForm.productionDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择生产日期"
          />
        </el-form-item>
        <el-form-item label="入库数量">
          <el-input-number v-model="batchForm.stock" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="batchSaving" @click="onStockIn">批次入库</el-button>
        </el-form-item>
      </el-form>
      <!-- 批次列表 -->
      <el-table v-loading="batchLoading" :data="batchList" border size="small">
        <el-table-column prop="batchNo" label="批次号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="productionDate" label="生产日期" width="110" />
        <el-table-column prop="expiryDate" label="到期日" width="110" />
        <el-table-column prop="initStock" label="初始库存" width="90" />
        <el-table-column prop="stock" label="剩余库存" width="90" />
        <el-table-column label="折扣" width="80">
          <template #default="{ row }">{{ row.discount != null ? row.discount : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="batchTagType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 溯源维护弹窗 -->
    <el-dialog v-model="traceVisible" :title="`溯源维护 - ${currentProduct?.name || ''}`" width="640px">
      <el-form :model="traceForm" label-width="110px">
        <el-form-item label="产地">
          <el-input v-model="traceForm.originPlace" placeholder="请输入产地" />
        </el-form-item>
        <el-form-item label="种植户/养殖户">
          <el-input v-model="traceForm.farmer" placeholder="请输入种植户/养殖户" />
        </el-form-item>
        <el-form-item label="种植日期">
          <el-date-picker v-model="traceForm.plantDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="采摘/出栏日期">
          <el-date-picker v-model="traceForm.pickDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="检测报告">
          <ImageUpload v-model="traceForm.testReport" />
        </el-form-item>
        <el-form-item label="运输/冷链信息">
          <el-input v-model="traceForm.transportInfo" type="textarea" :rows="3" placeholder="请输入运输/冷链信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="traceVisible = false">取消</el-button>
        <el-button type="primary" :loading="traceSaving" @click="onSaveTrace">保存</el-button>
      </template>
    </el-dialog>

    <!-- 库存预警弹窗 -->
    <el-dialog v-model="warnVisible" title="库存预警商品" width="700px">
      <el-table v-loading="warnLoading" :data="warnList" border size="small">
        <el-table-column prop="name" label="商品名称" min-width="130" show-overflow-tooltip />
        <el-table-column label="库存" width="100">
          <template #default="{ row }">
            <span class="stock-warn">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warnStock" label="预警库存" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '起售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="warning" @click="openBatch(row)">批次入库</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Warning } from '@element-plus/icons-vue'
import Pagination from '../../components/Pagination.vue'
import ImageUpload from '../../components/ImageUpload.vue'
import {
  getProductPage,
  addProduct,
  updateProduct,
  deleteProducts,
  updateProductStatus,
  getWarnList,
  getBatchList,
  batchStockIn,
  saveTrace,
  getProductDetail
} from '../../api/product'
import { getCategoryList } from '../../api/category'
import { imgUrl } from '../../utils'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, name: '', categoryId: null, status: null, freshType: null })
const categoryList = ref([])

// 保鲜类型展示
function freshText(t) {
  return { 0: '常温', 1: '冷藏', 2: '冷冻' }[t] ?? '-'
}
function freshTagType(t) {
  return { 0: 'success', 1: 'primary', 2: 'info' }[t] || 'info'
}
// 批次状态 tag 颜色
function batchTagType(s) {
  return { 1: 'success', 2: 'warning', 3: 'danger' }[s] || 'info'
}

// 加载商品分页
async function loadList() {
  loading.value = true
  try {
    const res = await getProductPage(query)
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

// ========== 新增/编辑商品 ==========
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const emptyForm = () => ({
  id: null,
  name: '',
  categoryId: null,
  price: null,
  priceType: 1,
  unit: '',
  freshType: 0,
  shelfLifeDays: null,
  stock: 0,
  warnStock: null,
  originPlace: '',
  image: '',
  description: ''
})
const form = reactive(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  priceType: [{ required: true, message: '请选择计价方式', trigger: 'change' }],
  freshType: [{ required: true, message: '请选择保鲜类型', trigger: 'change' }],
  stock: [{ required: true, message: '请输入总库存', trigger: 'blur' }]
}

// 打开编辑弹窗
function openEdit(row) {
  Object.assign(form, emptyForm())
  if (row) {
    Object.assign(form, {
      id: row.id,
      name: row.name,
      categoryId: row.categoryId,
      price: row.price,
      priceType: row.priceType,
      unit: row.unit || '',
      freshType: row.freshType,
      shelfLifeDays: row.shelfLifeDays,
      stock: row.stock,
      warnStock: row.warnStock,
      originPlace: row.originPlace,
      image: row.image || '',
      description: row.description
    })
  }
  editVisible.value = true
}

// 保存商品
async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateProduct(form)
    } else {
      await addProduct(form)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

// 起售/停售
async function onStatusChange(row, val) {
  await updateProductStatus(val ? 1 : 0, row.id)
  ElMessage.success(val ? '已起售' : '已停售')
  loadList()
}

// 删除商品
async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteProducts([row.id])
  ElMessage.success('删除成功')
  loadList()
}

// ========== 批次管理 ==========
const batchVisible = ref(false)
const batchLoading = ref(false)
const batchSaving = ref(false)
const batchList = ref([])
const currentProduct = ref(null)
const batchForm = reactive({ productionDate: '', stock: 1 })

// 打开批次弹窗并加载批次列表
async function openBatch(row) {
  currentProduct.value = row
  batchForm.productionDate = ''
  batchForm.stock = 1
  batchVisible.value = true
  loadBatchList()
}

async function loadBatchList() {
  batchLoading.value = true
  try {
    const res = await getBatchList(currentProduct.value.id)
    batchList.value = res.data || []
  } finally {
    batchLoading.value = false
  }
}

// 批次入库
async function onStockIn() {
  if (!batchForm.productionDate) {
    ElMessage.warning('请选择生产日期')
    return
  }
  batchSaving.value = true
  try {
    await batchStockIn({
      productId: currentProduct.value.id,
      productionDate: batchForm.productionDate,
      stock: batchForm.stock
    })
    ElMessage.success('入库成功')
    loadBatchList()
    loadList()
  } finally {
    batchSaving.value = false
  }
}

// ========== 溯源维护 ==========
const traceVisible = ref(false)
const traceSaving = ref(false)
const traceForm = reactive({
  productId: null,
  originPlace: '',
  farmer: '',
  plantDate: '',
  pickDate: '',
  testReport: '',
  transportInfo: ''
})

// 打开溯源弹窗：取商品详情预填已有溯源信息
async function openTrace(row) {
  currentProduct.value = row
  Object.assign(traceForm, {
    productId: row.id,
    originPlace: row.originPlace || '',
    farmer: '',
    plantDate: '',
    pickDate: '',
    testReport: '',
    transportInfo: ''
  })
  traceVisible.value = true
  const res = await getProductDetail(row.id)
  if (res.data && res.data.trace) {
    const t = res.data.trace
    Object.assign(traceForm, {
      originPlace: t.originPlace || '',
      farmer: t.farmer || '',
      plantDate: t.plantDate || '',
      pickDate: t.pickDate || '',
      testReport: t.testReport || '',
      transportInfo: t.transportInfo || ''
    })
  }
}

// 保存溯源
async function onSaveTrace() {
  traceSaving.value = true
  try {
    await saveTrace(traceForm)
    ElMessage.success('保存成功')
    traceVisible.value = false
  } finally {
    traceSaving.value = false
  }
}

// ========== 库存预警 ==========
const warnVisible = ref(false)
const warnLoading = ref(false)
const warnList = ref([])

async function openWarnList() {
  warnVisible.value = true
  warnLoading.value = true
  try {
    const res = await getWarnList()
    warnList.value = res.data || []
  } finally {
    warnLoading.value = false
  }
}

// 初始化：加载分类下拉 + 商品列表
onMounted(async () => {
  const res = await getCategoryList()
  categoryList.value = res.data || []
  loadList()
})
</script>

<style scoped>
.batch-in-form {
  margin-bottom: 8px;
}
</style>
