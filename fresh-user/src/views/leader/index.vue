<template>
  <div class="page">
    <NavBar title="团长专区" />
    <van-cell-group inset class="group">
      <van-cell title="发起团购" icon="add-o" is-link to="/leader/launch" label="选择商品、定团购价与成团人数" />
      <van-cell title="我发起的团购" icon="cluster-o" is-link to="/leader/list" label="查看进度与参团成员" />
      <van-cell title="自提核销" icon="certificate" is-link to="/leader/verify" label="输入用户自提码完成核销" />
    </van-cell-group>
  </div>
</template>

<script setup>
import NavBar from '../../components/NavBar.vue'
</script>

<style scoped>
.group {
  margin-top: 12px;
}
</style>
