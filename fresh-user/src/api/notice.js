import request from '../utils/request'

// 消息通知
export function noticeList() {
  return request.get('/user/notice/list')
}

// 标记已读
export function readNotice(id) {
  return request.put(`/user/notice/read/${id}`)
}
