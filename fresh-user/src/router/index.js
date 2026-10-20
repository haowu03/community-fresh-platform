import { createRouter, createWebHistory } from 'vue-router'

// 路由表：登录页公开，其余页面需登录
const routes = [
  {
    path: '/login',
    component: () => import('../views/login/index.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    // 带底部 TabBar 的主框架
    path: '/',
    component: () => import('../views/layout/index.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: () => import('../views/home/index.vue'), meta: { title: '首页' } },
      { path: 'category', component: () => import('../views/category/index.vue'), meta: { title: '分类' } },
      { path: 'cart', component: () => import('../views/cart/index.vue'), meta: { title: '购物车' } },
      { path: 'community', component: () => import('../views/community/index.vue'), meta: { title: '社区' } },
      { path: 'mine', component: () => import('../views/mine/index.vue'), meta: { title: '我的' } }
    ]
  },
  { path: '/detail/:id', component: () => import('../views/detail/index.vue'), meta: { title: '商品详情' } },
  { path: '/nearExpiry', component: () => import('../views/nearExpiry/index.vue'), meta: { title: '临期特价' } },
  { path: '/presale', component: () => import('../views/presale/index.vue'), meta: { title: '预售专区' } },
  { path: '/submit', component: () => import('../views/submit/index.vue'), meta: { title: '提交订单' } },
  { path: '/order', component: () => import('../views/order/index.vue'), meta: { title: '我的订单' } },
  { path: '/orderDetail/:id', component: () => import('../views/orderDetail/index.vue'), meta: { title: '订单详情' } },
  { path: '/evaluation', component: () => import('../views/evaluation/index.vue'), meta: { title: '评价' } },
  { path: '/address', component: () => import('../views/address/index.vue'), meta: { title: '地址管理' } },
  { path: '/addressEdit', component: () => import('../views/addressEdit/index.vue'), meta: { title: '编辑地址' } },
  { path: '/postDetail/:id', component: () => import('../views/postDetail/index.vue'), meta: { title: '帖子详情' } },
  { path: '/postPublish', component: () => import('../views/postPublish/index.vue'), meta: { title: '发帖' } },
  { path: '/myPosts', component: () => import('../views/myPosts/index.vue'), meta: { title: '我的帖子' } },
  { path: '/groupbuy', component: () => import('../views/groupbuy/index.vue'), meta: { title: '社区团购' } },
  { path: '/groupDetail/:id', component: () => import('../views/groupDetail/index.vue'), meta: { title: '团购详情' } },
  { path: '/myJoin', component: () => import('../views/myJoin/index.vue'), meta: { title: '我的参团' } },
  { path: '/notice', component: () => import('../views/notice/index.vue'), meta: { title: '消息通知' } },
  { path: '/leader', component: () => import('../views/leader/index.vue'), meta: { title: '团长专区' } },
  { path: '/leader/launch', component: () => import('../views/leader/launch.vue'), meta: { title: '发起团购' } },
  { path: '/leader/list', component: () => import('../views/leader/list.vue'), meta: { title: '我发起的团购' } },
  { path: '/leader/verify', component: () => import('../views/leader/verify.vue'), meta: { title: '自提核销' } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：无 token 跳登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('fresh-token')
  if (to.meta.public || token) {
    next()
  } else {
    next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

router.afterEach(to => {
  document.title = to.meta.title ? `${to.meta.title} - 社区生鲜` : '社区生鲜'
})

export default router
