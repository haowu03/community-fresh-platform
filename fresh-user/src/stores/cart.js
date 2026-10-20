import { defineStore } from 'pinia'
import { cartList } from '../api/cart'

// 购物车状态：用于 TabBar 角标与购物车页
export const useCartStore = defineStore('cart', {
  state: () => ({
    list: []
  }),
  getters: {
    count: s => s.list.length
  },
  actions: {
    async refresh() {
      try {
        this.list = (await cartList()) || []
      } catch (e) {
        // 未登录等情况静默处理
      }
    }
  }
})
