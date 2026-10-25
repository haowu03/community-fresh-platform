import request from '../utils/request'

// 订单条件分页搜索
export function searchOrders(params) {
  return request.get('/admin/order/conditionSearch', { params })
}

// 订单详情
export function getOrderDetail(id) {
  return request.get(`/admin/order/details/${id}`)
}

// 各状态订单数量统计
export function getOrderStatistics() {
  return request.get('/admin/order/statistics')
}

// 接单
export function confirmOrder(id) {
  return request.put('/admin/order/confirm', null, { params: { id } })
}

// 拒单
export function rejectOrder(data) {
  return request.put('/admin/order/rejection', data)
}

// 派送 / 备货完成
export function deliveryOrder(id) {
  return request.put(`/admin/order/delivery/${id}`)
}

// 完成订单
export function completeOrder(id) {
  return request.put(`/admin/order/complete/${id}`)
}

// 取消订单
export function cancelOrder(data) {
  return request.put('/admin/order/cancel', data)
}

// 自提码核销
export function verifyPickupCode(data) {
  return request.put('/admin/order/verify', data)
}
