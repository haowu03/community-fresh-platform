import request from '../utils/request'

// 营业额统计（begin/end: yyyy-MM-dd）
export function getTurnoverStatistics(params) {
  return request.get('/admin/report/turnoverStatistics', { params })
}

// 用户统计
export function getUserStatistics(params) {
  return request.get('/admin/report/userStatistics', { params })
}

// 订单统计
export function getOrderReport(params) {
  return request.get('/admin/report/orderStatistics', { params })
}

// 销量 TOP10
export function getTop10(params) {
  return request.get('/admin/report/top10', { params })
}

// 损耗统计（临期/过期批次金额）
export function getLossStatistics() {
  return request.get('/admin/report/lossStatistics')
}
