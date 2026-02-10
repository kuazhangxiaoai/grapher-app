import request from '@/utils/request'

// 查询ppt用户信息列表
export function listPptuser(query) {
  return request({
    url: '/user/pptuser/list',
    method: 'get',
    params: query
  })
}

// 查询ppt用户信息详细
export function getPptuser(userId) {
  return request({
    url: '/user/pptuser/' + userId,
    method: 'get'
  })
}

// 新增ppt用户信息
export function addPptuser(data) {
  return request({
    url: '/user/pptuser',
    method: 'post',
    data: data
  })
}

// 修改ppt用户信息
export function updatePptuser(data) {
  return request({
    url: '/user/pptuser',
    method: 'put',
    data: data
  })
}

// 删除ppt用户信息
export function delPptuser(userId) {
  return request({
    url: '/user/pptuser/' + userId,
    method: 'delete'
  })
}

// 查询授权数据库
export function getSettingDb(userId) {
  return request({
    url: '/user/pptuser/getSettingDb/' + userId,
    method: 'get'
  })
}

// 保存授权数据库
export function updateSettingDb(data) {
  return request({
    url: '/user/pptuser/getSettingDb/',
    method: 'put',
    params: data
  })
}