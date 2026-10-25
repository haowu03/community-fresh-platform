<template>
  <div class="page">
    <NavBar title="地址管理" />

    <div v-for="a in list" :key="a.id" class="card">
      <div class="head">
        <span class="name">{{ a.consignee }}</span>
        <span class="phone">{{ a.phone }}</span>
        <van-tag v-if="a.label" plain type="primary">{{ a.label }}</van-tag>
        <van-tag v-if="a.isDefault === 1" type="success">默认</van-tag>
      </div>
      <div class="detail">{{ a.provinceName }}{{ a.cityName }}{{ a.districtName }}{{ a.detail }}</div>
      <div class="ops">
        <van-button v-if="a.isDefault !== 1" size="mini" plain type="success" @click="onSetDefault(a)">设为默认</van-button>
        <van-button size="mini" plain @click="$router.push(`/addressEdit?id=${a.id}`)">编辑</van-button>
        <van-button size="mini" plain type="danger" @click="onDelete(a)">删除</van-button>
      </div>
    </div>

    <van-empty v-if="!list.length" description="暂无收货地址" />

    <div class="add-wrap">
      <van-button round block type="success" icon="plus" @click="$router.push('/addressEdit')">新增地址</van-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '../../components/NavBar.vue'
import { addressList, setDefaultAddress, deleteAddress } from '../../api/address'

const list = ref([])

async function load() {
  list.value = (await addressList()) || []
}

async function onSetDefault(a) {
  await setDefaultAddress(a.id)
  showToast('已设为默认地址')
  load()
}

async function onDelete(a) {
  await showConfirmDialog({ title: '提示', message: '确定删除该地址吗？' })
  await deleteAddress(a.id)
  showToast('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name {
  font-size: 15px;
  font-weight: 600;
}
.phone {
  color: #666;
  font-size: 13px;
}
.detail {
  margin-top: 6px;
  color: #666;
  font-size: 13px;
}
.ops {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
.add-wrap {
  padding: 16px 12px;
}
</style>
