import request from '../utils/request'

// 自提点分页查询
export function getPickupPage(params) {
  return request.get('/admin/pickup/point/page', { params })
}

// 新增自提点
export function addPickupPoint(data) {
  return request.post('/admin/pickup/point', data)
}

// 修改自提点
export function updatePickupPoint(data) {
  return request.put('/admin/pickup/point', data)
}

// 删除自提点
export function deletePickupPoint(id) {
  return request.delete('/admin/pickup/point', { params: { id } })
}

// 启用/停用自提点
export function updatePickupStatus(status, id) {
  return request.put(`/admin/pickup/point/status/${status}`, null, { params: { id } })
}

// 新增/修改自提时间段
export function saveTimeSlot(data) {
  return request.post('/admin/pickup/point/slot', data)
}

// 删除自提时间段
export function deleteTimeSlot(id) {
  return request.delete(`/admin/pickup/point/slot/${id}`)
}

// 自提点时间段列表
export function getTimeSlots(pointId) {
  return request.get('/admin/pickup/point/slot/list', { params: { pointId } })
}
