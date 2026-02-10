import request from '@/utils/request'

// 查询地图引擎列表
export function listPptengine(query) {
  return request({
    url: '/engine/pptengine/list',
    method: 'get',
    params: query
  })
}

// 查询地图引擎详细
export function getPptengine(id) {
  return request({
    url: '/engine/pptengine/' + id,
    method: 'get'
  })
}

// 新增地图引擎
export function addPptengine(data) {
  return request({
    url: '/engine/pptengine',
    method: 'post',
    data: data
  })
}

// 修改地图引擎
export function updatePptengine(data) {
  return request({
    url: '/engine/pptengine',
    method: 'put',
    data: data
  })
}

// 删除地图引擎
export function delPptengine(id) {
  return request({
    url: '/engine/pptengine/' + id,
    method: 'delete'
  })
}
