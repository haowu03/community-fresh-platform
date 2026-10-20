import request from '../utils/request'

// 团购活动分页
export function getGroupBuyPage(params) {
  return request.get('/admin/groupBuy/page', { params })
}

// 参团成员列表
export function getGroupBuyMembers(id) {
  return request.get(`/admin/groupBuy/${id}/members`)
}

// 手动截团
export function closeGroupBuy(id) {
  return request.put(`/admin/groupBuy/close/${id}`)
}
