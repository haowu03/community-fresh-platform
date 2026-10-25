import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// axios 实例：统一处理 token 与响应 code
const request = axios.create({
  baseURL: '/',
  timeout: 15000
})

// 请求拦截器：携带管理端 token（原始 token，不加 Bearer 前缀）
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('fresh-admin-token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：code===1 成功返回 data，否则提示 msg；401 跳登录
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 1) {
      return res
    }
    ElMessage.error(res.msg || '操作失败')
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // token 失效：清除并跳登录页
      localStorage.removeItem('fresh-admin-token')
      localStorage.removeItem('fresh-admin-name')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
