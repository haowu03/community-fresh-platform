import request from '../utils/request'

// 预售活动（用户端）
export function presaleList() {
  return request.get('/user/presale/list')
}

export function presaleDetail(id) {
  return request.get(`/user/presale/${id}`)
}
