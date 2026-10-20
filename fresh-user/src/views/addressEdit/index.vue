<template>
  <div class="page">
    <NavBar :title="form.id ? '编辑地址' : '新增地址'" />
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="form.consignee" label="收货人" placeholder="请输入收货人姓名"
          :rules="[{ required: true, message: '请输入收货人' }]" />
        <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }]" />
      </van-cell-group>

      <van-cell-group inset title="所在地区">
        <van-field v-model="form.provinceName" label="省" placeholder="如 浙江省"
          :rules="[{ required: true, message: '请输入省' }]" />
        <van-field v-model="form.cityName" label="市" placeholder="如 杭州市"
          :rules="[{ required: true, message: '请输入市' }]" />
        <van-field v-model="form.districtName" label="区/县" placeholder="如 西湖区"
          :rules="[{ required: true, message: '请输入区/县' }]" />
        <van-field v-model="form.detail" label="详细地址" type="textarea" rows="2" placeholder="小区、楼栋、门牌号"
          :rules="[{ required: true, message: '请输入详细地址' }]" />
      </van-cell-group>

      <van-cell-group inset title="标签">
        <van-field label="标签">
          <template #input>
            <van-radio-group v-model="form.label" direction="horizontal">
              <van-radio name="家" checked-color="#39b57f">家</van-radio>
              <van-radio name="公司" checked-color="#39b57f">公司</van-radio>
              <van-radio name="学校" checked-color="#39b57f">学校</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field label="设为默认">
          <template #input>
            <van-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" size="20" active-color="#39b57f" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="success" native-type="submit" :loading="saving">保存</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { addressList, addAddress, updateAddress } from '../../api/address'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const form = reactive({
  id: null,
  consignee: '',
  phone: '',
  provinceName: '',
  cityName: '',
  districtName: '',
  detail: '',
  label: '家',
  isDefault: 0
})

async function onSubmit() {
  saving.value = true
  try {
    if (form.id) {
      await updateAddress({ ...form })
      showToast('修改成功')
    } else {
      await addAddress({ ...form })
      showToast('新增成功')
    }
    router.back()
  } finally {
    saving.value = false
  }
}

// 编辑场景：后端无单条查询接口，从列表中取
onMounted(async () => {
  const id = route.query.id
  if (id) {
    const list = (await addressList()) || []
    const found = list.find(a => String(a.id) === String(id))
    if (found) Object.assign(form, found)
  }
})
</script>

<style scoped>
.submit-wrap {
  padding: 16px;
}
</style>
