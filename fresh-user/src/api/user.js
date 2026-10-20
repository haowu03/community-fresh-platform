import request from '../utils/request'

// 用户模块：登录/注册/个人信息
export function login(data) {
  return request.post('/user/user/login', data)
}

export function register(data) {
  return request.post('/user/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/user/info')
}

export function updateUserInfo(data) {
  return request.put('/user/user', data)
}

// 文件上传（头像等）：multipart/form-data
export function uploadFile(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/user/common/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
