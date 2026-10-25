// 通用工具函数

// 图片完整地址：相对路径拼接后端前缀
export function imgUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return 'http://localhost:8080' + path
}

// 补零
function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

// 格式化 Date 为 yyyy-MM-dd
export function formatDate(d) {
  if (!d) return ''
  const date = d instanceof Date ? d : new Date(d)
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

// 格式化 Date 为 yyyy-MM-dd HH:mm:ss
export function formatDateTime(d) {
  if (!d) return ''
  const date = d instanceof Date ? d : new Date(d)
  return `${formatDate(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 近 N 天的 [begin, end] 日期字符串（yyyy-MM-dd）
export function lastDaysRange(n = 7) {
  const end = new Date()
  const begin = new Date()
  begin.setDate(begin.getDate() - (n - 1))
  return [formatDate(begin), formatDate(end)]
}
