import { defineStore } from 'pinia'

// 当前登录员工信息（token + 姓名），持久化到 localStorage
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fresh-admin-token') || '',
    name: localStorage.getItem('fresh-admin-name') || '',
    id: localStorage.getItem('fresh-admin-id') || ''
  }),
  actions: {
    // 登录成功保存
    setLogin(data) {
      this.token = data.token
      this.name = data.name
      this.id = data.id
      localStorage.setItem('fresh-admin-token', data.token)
      localStorage.setItem('fresh-admin-name', data.name)
      localStorage.setItem('fresh-admin-id', data.id)
    },
    // 退出登录清除
    logout() {
      this.token = ''
      this.name = ''
      this.id = ''
      localStorage.removeItem('fresh-admin-token')
      localStorage.removeItem('fresh-admin-name')
      localStorage.removeItem('fresh-admin-id')
    }
  }
})
