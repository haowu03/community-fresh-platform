import request from '../utils/request'

// 自提点（用户端）
export function pickupPointList() {
  return request.get('/user/pickup/point/list')
}

// 自提点可用时间段
export function pickupSlots(id) {
  return request.get(`/user/pickup/point/${id}/slots`)
}
