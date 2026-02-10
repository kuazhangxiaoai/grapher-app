import request from '@/utils/request'

// 查询数据源信息列表
export function listPptdatabase(query) {
  return request({
    url: '/database/pptdatabase/list',
    method: 'get',
    params: query
  })
}

// 查询数据源信息详细
export function getPptdatabase(id) {
  return request({
    url: '/database/pptdatabase/' + id,
    method: 'get'
  })
}

// 新增数据源信息
export function addPptdatabase(data) {
  return request({
    url: '/database/pptdatabase',
    method: 'post',
    data: data
  })
}

// 修改数据源信息
export function updatePptdatabase(data) {
  return request({
    url: '/database/pptdatabase',
    method: 'put',
    data: data
  })
}

// 删除数据源信息
export function delPptdatabase(id) {
  return request({
    url: '/database/pptdatabase/' + id,
    method: 'delete'
  })
}
