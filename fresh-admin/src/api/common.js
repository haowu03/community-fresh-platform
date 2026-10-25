import request from '../utils/request'

// 图片/文件上传（multipart，字段名 file），返回 /upload/xxx.jpg
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
