import request from '../utils/request'

// 商品分页查询
export function getProductPage(params) {
  return request.get('/admin/product/page', { params })
}

// 商品详情（含批次、溯源）
export function getProductDetail(id) {
  return request.get(`/admin/product/${id}`)
}

// 新增商品
export function addProduct(data) {
  return request.post('/admin/product', data)
}

// 修改商品
export function updateProduct(data) {
  return request.put('/admin/product', data)
}

// 批量删除商品
export function deleteProducts(ids) {
  return request.delete('/admin/product', { params: { ids } })
}

// 起售/停售
export function updateProductStatus(status, id) {
  return request.put(`/admin/product/status/${status}`, null, { params: { id } })
}

// 库存预警列表
export function getWarnList() {
  return request.get('/admin/product/warnList')
}

// 批次入库
export function batchStockIn(data) {
  return request.post('/admin/product/batch', data)
}

// 商品批次列表
export function getBatchList(productId) {
  return request.get('/admin/product/batch/list', { params: { productId } })
}

// 维护溯源信息
export function saveTrace(data) {
  return request.put('/admin/product/trace', data)
}
