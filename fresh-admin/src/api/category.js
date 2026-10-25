import request from '../utils/request'

// 分类分页查询
export function getCategoryPage(params) {
  return request.get('/admin/category/page', { params })
}

// 启用分类列表（下拉用）
export function getCategoryList() {
  return request.get('/admin/category/list')
}

// 新增分类
export function addCategory(data) {
  return request.post('/admin/category', data)
}

// 修改分类
export function updateCategory(data) {
  return request.put('/admin/category', data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete('/admin/category', { params: { id } })
}

// 启用/禁用分类
export function updateCategoryStatus(status, id) {
  return request.put(`/admin/category/status/${status}`, null, { params: { id } })
}
