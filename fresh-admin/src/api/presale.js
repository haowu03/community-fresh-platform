import request from '../utils/request'

// 预售活动分页查询
export function getPresalePage(params) {
  return request.get('/admin/presale/page', { params })
}

// 发布预售活动
export function publishPresale(data) {
  return request.post('/admin/presale', data)
}

// 修改预售活动
export function updatePresale(data) {
  return request.put('/admin/presale', data)
}

// 提前结束预售活动
export function closePresale(id) {
  return request.put(`/admin/presale/close/${id}`)
}

// 删除预售活动
export function deletePresale(id) {
  return request.delete('/admin/presale', { params: { id } })
}
