import request from '../utils/request'

// 社区帖子（用户端）
export function postPage(params) {
  return request.get('/user/post/page', { params })
}

// 帖子详情：返回 { post, comments }
export function postDetail(id) {
  return request.get(`/user/post/${id}`)
}

// 发帖：{ type, title, content, images(JSON数组字符串) }
export function publishPost(data) {
  return request.post('/user/post', data)
}

// 我的帖子（全部状态）
export function myPosts() {
  return request.get('/user/post/my')
}

export function deletePost(id) {
  return request.delete(`/user/post/${id}`)
}

// 评论：{ postId, content, parentId }
export function commentPost(data) {
  return request.post('/user/post/comment', data)
}

// 点赞 / 取消点赞（切换）
export function likePost(id) {
  return request.post(`/user/post/like/${id}`)
}
