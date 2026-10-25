import request from '../utils/request'

// 购物车
export function cartList() {
  return request.get('/user/shoppingCart/list')
}

// 加入购物车：按份传 number，按重量传 weight
export function addCart(data) {
  return request.post('/user/shoppingCart/add', data)
}

// 减少一份 / 删除条目
export function subCart(data) {
  return request.post('/user/shoppingCart/sub', data)
}

export function cleanCart() {
  return request.delete('/user/shoppingCart/clean')
}
