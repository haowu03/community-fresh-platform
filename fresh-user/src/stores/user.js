import { defineStore } from 'pinia'
import { getUserInfo } from '../api/user'

// 用户状态：token + 用户信息（持久化到 localStorage）
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fresh-token') || '',
    userInfo: JSON.parse(localStorage.getItem('fresh-user') || 'null')
  }),
  getters: {
    // 角色：1普通用户 2团长
    isLeader: s => (s.userInfo && s.userInfo.role === 2)
  },
  actions: {
    // 登录成功后保存 token 与用户信息
    setLogin(data) {
      this.token = data.token
      this.userInfo = { id: data.id, username: data.username, name: data.name, role: data.role }
      localStorage.setItem('fresh-token', data.token)
      localStorage.setItem('fresh-user', JSON.stringify(this.userInfo))
    },
    // 拉取最新用户信息
    async fetchInfo() {
      const data = await getUserInfo()
      if (data) {
        this.userInfo = { ...(this.userInfo || {}), ...data }
        localStorage.setItem('fresh-user', JSON.stringify(this.userInfo))
      }
    },
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('fresh-token')
      localStorage.removeItem('fresh-user')
    }
  }
})
