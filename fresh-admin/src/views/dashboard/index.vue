<template>
  <div class="page-container">
    <!-- 损耗统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">正常批次库存货值</div>
          <div class="stat-value">¥ {{ loss.normalAmount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">临期批次金额</div>
          <div class="stat-value warn">¥ {{ loss.nearExpiryAmount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">过期批次损耗金额</div>
          <div class="stat-value danger">¥ {{ loss.expiredAmount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">临期 / 过期批次数</div>
          <div class="stat-value">
            <span class="warn">{{ loss.nearExpiryCount ?? 0 }}</span>
            <span class="divider">/</span>
            <span class="danger">{{ loss.expiredCount ?? 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日期范围选择 -->
    <el-card class="search-card">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        :clearable="false"
        @change="loadAll"
      />
      <el-button type="primary" style="margin-left: 12px" @click="loadAll">查询</el-button>
    </el-card>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>营业额统计（元）</template>
          <div ref="turnoverRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>用户统计</template>
          <div ref="userRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            订单统计
            <span v-if="orderStat" class="order-summary">
              总订单 {{ orderStat.totalOrderCount }}，有效 {{ orderStat.validOrderCount }}，完成率
              {{ ((orderStat.orderCompletionRate || 0) * 100).toFixed(1) }}%
            </span>
          </template>
          <div ref="orderRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>销量 TOP10</template>
          <div ref="top10Ref" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getTurnoverStatistics,
  getUserStatistics,
  getOrderReport,
  getTop10,
  getLossStatistics
} from '../../api/report'
import { lastDaysRange } from '../../utils'

// echarts 按需注册
echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

// 日期范围：默认近 7 日
const dateRange = ref(lastDaysRange(7))
const loss = ref({})
const orderStat = ref(null)

const turnoverRef = ref()
const userRef = ref()
const orderRef = ref()
const top10Ref = ref()

// 保存图表实例
const charts = []

// 初始化/更新图表
function renderChart(el, option) {
  let chart = echarts.getInstanceByDom(el)
  if (!chart) {
    chart = echarts.init(el)
    charts.push(chart)
  }
  chart.setOption(option)
}

// 逗号分隔字符串转数组
function splitList(str) {
  return str ? str.split(',') : []
}

// 加载全部报表数据
async function loadAll() {
  const [begin, end] = dateRange.value
  const params = { begin, end }

  // 并行请求 5 个报表接口
  const [turnoverRes, userRes, orderRes, top10Res, lossRes] = await Promise.all([
    getTurnoverStatistics(params),
    getUserStatistics(params),
    getOrderReport(params),
    getTop10(params),
    getLossStatistics()
  ])

  // 损耗卡片
  loss.value = lossRes.data || {}

  // 营业额折线
  const t = turnoverRes.data || {}
  renderChart(turnoverRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: splitList(t.dateList) },
    yAxis: { type: 'value' },
    series: [{ name: '营业额', type: 'line', smooth: true, areaStyle: {}, data: splitList(t.turnoverList).map(Number) }]
  })

  // 用户统计折线
  const u = userRes.data || {}
  renderChart(userRef.value, {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: splitList(u.dateList) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '用户总量', type: 'line', smooth: true, data: splitList(u.totalUserList).map(Number) },
      { name: '新增用户', type: 'line', smooth: true, data: splitList(u.newUserList).map(Number) }
    ]
  })

  // 订单统计
  const o = orderRes.data || {}
  orderStat.value = o
  renderChart(orderRef.value, {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: splitList(o.dateList) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '订单总数', type: 'line', smooth: true, data: splitList(o.orderCountList).map(Number) },
      { name: '有效订单', type: 'line', smooth: true, data: splitList(o.validOrderCountList).map(Number) }
    ]
  })

  // 销量 TOP10 柱状图
  const s = top10Res.data || {}
  renderChart(top10Ref.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 80 },
    xAxis: { type: 'category', data: splitList(s.nameList), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ name: '销量', type: 'bar', barMaxWidth: 30, itemStyle: { color: '#67c23a' }, data: splitList(s.numberList).map(Number) }]
  })
}

// 窗口缩放时重绘
function onResize() {
  charts.forEach((c) => c.resize())
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  charts.forEach((c) => c.dispose())
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}

.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-value.warn,
.warn {
  color: #e6a23c;
}

.stat-value.danger,
.danger {
  color: #f56c6c;
}

.divider {
  margin: 0 6px;
  color: #dcdfe6;
}

.chart-card {
  margin-bottom: 16px;
}

.chart {
  height: 320px;
}

.order-summary {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}
</style>
