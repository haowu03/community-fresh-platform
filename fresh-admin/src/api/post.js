import request from '../utils/request'

// 帖子分页（审核列表）
export function getPostPage(params) {
  return request.get('/admin/post/page', { params })
}

// 审核帖子（通过/拒绝）
export function auditPost(data) {
  return request.put('/admin/post/audit', data)
}
