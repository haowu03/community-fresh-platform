// 通用格式化与状态映射工具

// 后端静态资源前缀（商品/帖子图片是相对路径 /upload/xxx）
const IMG_BASE = 'http://localhost:8080'

// 占位图（灰色 svg）
const PLACEHOLDER =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect width="100%" height="100%" fill="#f0f0f0"/><text x="50%" y="52%" font-size="14" fill="#bbb" text-anchor="middle">暂无图片</text></svg>'
  )

/** 图片地址拼接 */
export function imgUrl(path) {
  if (!path) return PLACEHOLDER
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('data:')) return path
  return IMG_BASE + path
}

/** 帖子 images 字段（JSON 数组字符串）解析 */
export function parseImages(images) {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
}

/** 保鲜类型映射：0常温(橙) 1冷藏(蓝) 2冷冻(深蓝) */
export const freshTypeMap = {
  0: { text: '常温', color: '#ff7a29' },
  1: { text: '冷藏', color: '#1989fa' },
  2: { text: '冷冻', color: '#0050b3' }
}

export function freshType(type) {
  return freshTypeMap[type] || { text: '未知', color: '#999' }
}

/** 订单状态映射：1待付款 2待接单 3备货中 4配送中/待自提 5已完成 6已取消 */
export function orderStatusText(status, deliveryType) {
  switch (status) {
    case 1: return '待付款'
    case 2: return '待接单'
    case 3: return '备货中'
    case 4: return deliveryType === 2 ? '待自提' : '配送中'
    case 5: return '已完成'
    case 6: return '已取消'
    default: return '未知'
  }
}

export function orderStatusColor(status) {
  switch (status) {
    case 1: return '#ff976a'
    case 2: case 3: case 4: return '#1989fa'
    case 5: return '#39b57f'
    case 6: return '#999'
    default: return '#999'
  }
}

/** 订单类型映射：1普通 2预售 3团购 */
export function orderTypeText(type) {
  return { 1: '普通订单', 2: '预售订单', 3: '团购订单' }[type] || '普通订单'
}

/** 帖子审核状态：0待审核 1已通过 2已拒绝 */
export const postStatusMap = {
  0: { text: '待审核', color: '#ff976a' },
  1: { text: '已通过', color: '#39b57f' },
  2: { text: '已拒绝', color: '#ee0a24' }
}

/** 帖子类型：1团购分享 2好物分享 */
export function postTypeText(type) {
  return type === 1 ? '团购分享' : '好物分享'
}

/** 团购活动状态：0未开始 1进行中 2已成团 3已完成 4已流团 */
export const groupStatusMap = {
  0: { text: '未开始', color: '#ff976a' },
  1: { text: '进行中', color: '#39b57f' },
  2: { text: '已成团', color: '#1989fa' },
  3: { text: '已完成', color: '#999' },
  4: { text: '已流团', color: '#ee0a24' }
}

/** 消息通知类型：1自提通知 2成团通知 3流团退款通知 4系统通知 */
export function noticeTypeText(type) {
  return { 1: '自提通知', 2: '成团通知', 3: '退款通知', 4: '系统通知' }[type] || '通知'
}

/** 时间格式化：ISO(2026-10-19T23:57:00) → 2026-10-19 23:57 */
export function fmtTime(str) {
  if (!str) return ''
  return String(str).replace('T', ' ').slice(0, 16)
}

/** 日期格式化：→ 2026-10-19 */
export function fmtDate(str) {
  if (!str) return ''
  return String(str).replace('T', ' ').slice(0, 10)
}

/** 时间段格式化：09:00:00 → 09:00 */
export function fmtSlot(str) {
  if (!str) return ''
  return String(str).slice(0, 5)
}
