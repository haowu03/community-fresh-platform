<template>
  <div class="page">
    <NavBar title="发起团购" />
    <van-form @submit="onSubmit">
      <!-- 选择商品 -->
      <van-cell-group inset title="选择商品">
        <van-search v-model="keyword" placeholder="输入商品名称搜索" @search="search" @clear="search" />
        <van-cell v-if="selected" :title="selected.name" :label="`现价 ¥${selected.price}/${selected.unit || '份'}`">
          <template #right-icon><van-tag type="success">已选择</van-tag></template>
        </van-cell>
        <template v-else>
          <van-cell
            v-for="p in searchResults"
            :key="p.id"
            :title="p.name"
            :label="`¥${p.price}/${p.unit || '份'}`"
            is-link
            @click="selected = p"
          />
          <van-cell v-if="searched && !searchResults.length" title="未找到相关商品" title-style="color:#999" />
        </template>
        <van-cell v-if="selected" title="重新选择" is-link title-style="color:#1989fa" @click="selected = null" />
      </van-cell-group>

      <!-- 团购参数 -->
      <van-cell-group inset title="团购参数">
        <van-field v-model="form.groupPrice" type="number" label="团购价" placeholder="请输入团购价"
          :rules="[{ required: true, message: '请输入团购价' }]" />
        <van-field v-model="form.minNum" type="digit" label="成团人数" placeholder="请输入成团人数"
          :rules="[{ required: true, message: '请输入成团人数' }]" />
        <van-field :model-value="form.startTime" is-link readonly label="开始时间" placeholder="选择开始时间"
          :rules="[{ required: true, message: '请选择开始时间' }]" @click="openPicker('startTime')" />
        <van-field :model-value="form.endTime" is-link readonly label="截止时间" placeholder="选择截止时间"
          :rules="[{ required: true, message: '请选择截止时间' }]" @click="openPicker('endTime')" />
        <van-field label="自提点" :model-value="pointName" placeholder="自动带出绑定自提点" readonly />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="success" native-type="submit" :loading="submitting">发起团购</van-button>
      </div>
    </van-form>

    <!-- 日期时间选择弹层：日期 + 时间 -->
    <van-popup v-model:show="showPicker" position="bottom" round>
      <div class="picker-panel">
        <van-date-picker v-model="pickerDate" title="选择日期" :min-date="minDate" />
        <van-time-picker v-model="pickerTime" title="选择时间" />
        <van-button round block type="success" class="picker-confirm" @click="confirmPicker">确认</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { productList } from '../../api/product'
import { pickupPointList } from '../../api/pickup'
import { launchGroupBuy } from '../../api/groupbuy'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const user = useUserStore()

const keyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const selected = ref(null)
const form = reactive({ groupPrice: '', minNum: '', startTime: '', endTime: '' })
const myPoint = ref(null)
const submitting = ref(false)

const pointName = computed(() => (myPoint.value ? myPoint.value.name : '未绑定自提点，请联系管理员'))

// 搜索商品
async function search() {
  if (!keyword.value.trim()) {
    searchResults.value = []
    searched.value = false
    return
  }
  const data = await productList({ name: keyword.value.trim(), page: 1, pageSize: 20 })
  searchResults.value = (data && data.records) || []
  searched.value = true
}

// 日期时间选择
const showPicker = ref(false)
const pickerField = ref('startTime')
const pickerDate = ref([])
const pickerTime = ref(['12', '00'])
const minDate = new Date()

function openPicker(field) {
  pickerField.value = field
  // 默认选中今天 12:00
  const now = new Date()
  pickerDate.value = [
    String(now.getFullYear()),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0')
  ]
  pickerTime.value = ['12', '00']
  showPicker.value = true
}

// 组合为后端 LocalDateTime 可解析的 ISO 字符串
function confirmPicker() {
  const [y, m, d] = pickerDate.value
  const [hh, mm] = pickerTime.value
  if (!y) {
    showToast('请选择日期')
    return
  }
  form[pickerField.value] = `${y}-${m}-${d}T${hh}:${mm}:00`
  showPicker.value = false
}

async function onSubmit() {
  if (!selected.value) {
    showToast('请先选择商品')
    return
  }
  if (!myPoint.value) {
    showToast('未找到您绑定的自提点')
    return
  }
  if (form.endTime <= form.startTime) {
    showToast('截止时间需晚于开始时间')
    return
  }
  submitting.value = true
  try {
    await launchGroupBuy({
      productId: selected.value.id,
      pickupPointId: myPoint.value.id,
      groupPrice: Number(form.groupPrice),
      minNum: Number(form.minNum),
      startTime: form.startTime,
      endTime: form.endTime
    })
    showToast('发起团购成功')
    router.replace('/leader/list')
  } finally {
    submitting.value = false
  }
}

// 自动带出当前团长绑定的自提点（leaderId === 当前用户id）
onMounted(async () => {
  const points = (await pickupPointList()) || []
  myPoint.value = points.find(p => user.userInfo && p.leaderId === user.userInfo.id) || null
})
</script>

<style scoped>
.submit-wrap {
  padding: 16px;
}
.picker-panel {
  padding-bottom: 16px;
}
.picker-confirm {
  width: 90%;
  margin: 12px auto 0;
}
</style>
