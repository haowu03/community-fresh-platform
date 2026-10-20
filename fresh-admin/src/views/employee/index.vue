<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="员工姓名">
          <el-input v-model="query.name" placeholder="请输入员工姓名" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openEdit()">新增员工</el-button>
          <el-button type="warning" :icon="Key" @click="pwdVisible = true">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 员工表格 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="登录账号" min-width="120" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ { 1: '男', 2: '女' }[row.sex] || row.sex || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="(val) => onStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination v-model:page="query.page" v-model:pageSize="query.pageSize" :total="total" @change="loadList" />
    </el-card>

    <!-- 新增/编辑员工弹窗 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑员工' : '新增员工'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio value="1">男</el-radio>
            <el-radio value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idNumber" placeholder="请输入身份证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改自己密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="pwdForm.confirm" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="onEditPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Key } from '@element-plus/icons-vue'
import Pagination from '../../components/Pagination.vue'
import {
  getEmployeePage,
  addEmployee,
  updateEmployee,
  updateEmployeeStatus,
  editPassword
} from '../../api/employee'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, name: '' })

// 加载员工分页
async function loadList() {
  loading.value = true
  try {
    const res = await getEmployeePage(query)
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
const form = reactive({ id: null, username: '', name: '', phone: '', sex: '1', idNumber: '' })

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function openEdit(row) {
  Object.assign(form, {
    id: row ? row.id : null,
    username: row ? row.username : '',
    name: row ? row.name : '',
    phone: row ? row.phone : '',
    sex: row ? String(row.sex || '1') : '1',
    idNumber: row ? row.idNumber || '' : ''
  })
  editVisible.value = true
}

// 保存员工
async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateEmployee(form)
    } else {
      await addEmployee(form)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

// 启用/禁用
async function onStatusChange(row, val) {
  await updateEmployeeStatus(val ? 1 : 0, row.id)
  ElMessage.success(val ? '已启用' : '已禁用')
  loadList()
}

// ========== 修改密码 ==========
const pwdVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (r, v, cb) => (v === pwdForm.newPassword ? cb() : cb(new Error('两次密码输入不一致'))),
      trigger: 'blur'
    }
  ]
}

// 提交修改密码
async function onEditPassword() {
  await pwdFormRef.value.validate()
  pwdSaving.value = true
  try {
    await editPassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdVisible.value = false
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirm: '' })
  } finally {
    pwdSaving.value = false
  }
}

onMounted(loadList)
</script>
