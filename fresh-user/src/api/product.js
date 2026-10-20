import request from '../utils/request'

// 分类 / 商品浏览
export function categoryList() {
  return request.get('/user/category/list')
}

export function productList(params) {
  return request.get('/user/product/list', { params })
}

export function productDetail(id) {
  return request.get(`/user/product/${id}`)
}

// 临期特价专区
export function nearExpiryList() {
  return request.get('/user/product/nearExpiry')
}

// 商品溯源信息
export function productTrace(id) {
  return request.get(`/user/product/${id}/trace`)
}
