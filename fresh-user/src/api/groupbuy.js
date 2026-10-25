import request from '../utils/request'

// 社区团购（用户端 + 团长）
export function groupBuyList() {
  return request.get('/user/groupBuy/list')
}

export function groupBuyDetail(id) {
  return request.get(`/user/groupBuy/${id}`)
}

// 我参的团
export function myJoin() {
  return request.get('/user/groupBuy/myJoin')
}

// 发起团购（团长）：{ productId, pickupPointId, groupPrice, minNum, startTime, endTime }
export function launchGroupBuy(data) {
  return request.post('/user/groupBuy', data)
}

// 我发起的团购（团长）
export function myLaunch() {
  return request.get('/user/groupBuy/myLaunch')
}

// 参团成员（团长）
export function groupMembers(id) {
  return request.get(`/user/groupBuy/${id}/members`)
}

// 自提码核销（团长）：{ pickupCode }
export function verifyPickup(data) {
  return request.put('/user/groupBuy/verify', data)
}
