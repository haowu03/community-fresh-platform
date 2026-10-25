import request from '../utils/request'

// 订单（用户端）
export function submitOrder(data) {
  return request.post('/user/order/submit', data)
}

// 模拟支付：{ orderNumber, payMethod }
export function payOrder(data) {
  return request.put('/user/order/payment', data)
}

// 历史订单分页：{ page, pageSize, status }
export function historyOrders(params) {
  return request.get('/user/order/historyOrders', { params })
}

export function orderDetail(id) {
  return request.get(`/user/order/orderDetail/${id}`)
}

export function cancelOrder(id) {
  return request.put(`/user/order/cancel/${id}`)
}

export function repetitionOrder(id) {
  return request.post(`/user/order/repetition/${id}`)
}

export function reminderOrder(id) {
  return request.get(`/user/order/reminder/${id}`)
}

// 提交评价：{ orderId, productId, score, content, images }
export function submitEvaluation(data) {
  return request.post('/user/order/evaluation', data)
}
