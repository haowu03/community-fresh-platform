import { createRouter, createWebHistory } from 'vue-router'

// 路由：/login 独立页，其余挂在 Layout 下
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/index.vue'), meta: { title: '数据看板' } },
      { path: 'category', name: 'Category', component: () => import('../views/category/index.vue'), meta: { title: '分类管理' } },
      { path: 'product', name: 'Product', component: () => import('../views/product/index.vue'), meta: { title: '商品管理' } },
      { path: 'order', name: 'Order', component: () => import('../views/order/index.vue'), meta: { title: '订单管理' } },
      { path: 'pickup', name: 'Pickup', component: () => import('../views/pickup/index.vue'), meta: { title: '自提点管理' } },
      { path: 'presale', name: 'Presale', component: () => import('../views/presale/index.vue'), meta: { title: '预售管理' } },
      { path: 'groupbuy', name: 'GroupBuy', component: () => import('../views/groupbuy/index.vue'), meta: { title: '团购管理' } },
      { path: 'post', name: 'Post', component: () => import('../views/post/index.vue'), meta: { title: '帖子审核' } },
      { path: 'employee', name: 'Employee', component: () => import('../views/employee/index.vue'), meta: { title: '员工管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：无 token 一律跳登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('fresh-admin-token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
