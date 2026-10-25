import request from '../utils/request'

// 地址簿
export function addressList() {
  return request.get('/user/addressBook/list')
}

export function defaultAddress() {
  return request.get('/user/addressBook/default')
}

export function addAddress(data) {
  return request.post('/user/addressBook', data)
}

export function updateAddress(data) {
  return request.put('/user/addressBook', data)
}

// 设为默认：后端从 body 中取 id
export function setDefaultAddress(id) {
  return request.put('/user/addressBook/default', { id })
}

export function deleteAddress(id) {
  return request.delete('/user/addressBook', { params: { id } })
}
