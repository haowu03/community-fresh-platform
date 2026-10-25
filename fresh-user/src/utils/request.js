import axios from 'axios'
import { showToast } from 'vant'
import router from '../router'

// axios 实例：统一携带 token、统一处理 {code,msg,data} 响应
const request = axios.create({
  baseURL: '/',
  timeout: 15000
})

// 请求拦截器：携带原始 token（不加 Bearer 前缀）
request.interceptors.request.use(config => {
  const token = localStorage.getItem('fresh-token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

// 响应拦截器：code===1 取 data，否则 Toast 提示；401 跳登录
request.interceptors.response.use(
  res => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 1) {
        return body.data
      }
      showToast(body.msg || '操作失败')
      return Promise.reject(new Error(body.msg || '操作失败'))
    }
    return body
  },
  err => {
    if (err.response && err.response.status === 401) {
      localStorage.removeItem('fresh-token')
      localStorage.removeItem('fresh-user')
      if (router.currentRoute.value.path !== '/login') {
        showToast('登录已失效，请重新登录')
        router.replace('/login')
      }
    } else {
      showToast((err.response && err.response.data && err.response.data.msg) || err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default request
