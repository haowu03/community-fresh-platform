import request from '../utils/request'

// 员工登录
export function login(data) {
  return request.post('/admin/employee/login', data)
}

// 退出登录
export function logout() {
  return request.post('/admin/employee/logout')
}

// 员工分页查询
export function getEmployeePage(params) {
  return request.get('/admin/employee/page', { params })
}

// 新增员工
export function addEmployee(data) {
  return request.post('/admin/employee', data)
}

// 修改员工
export function updateEmployee(data) {
  return request.put('/admin/employee', data)
}

// 启用/禁用员工
export function updateEmployeeStatus(status, id) {
  return request.put(`/admin/employee/status/${status}`, null, { params: { id } })
}

// 修改当前登录员工密码
export function editPassword(data) {
  return request.put('/admin/employee/editPassword', data)
}
