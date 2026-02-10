import request from '@/utils/request'

// 查询项目管理列表
export function listPptproject(query) {
  return request({
    url: '/project/pptproject/list',
    method: 'get',
    params: query
  })
}

// 查询项目管理详细
export function getPptproject(pptProjectId) {
  return request({
    url: '/project/pptproject/' + pptProjectId,
    method: 'get'
  })
}

// 新增项目管理
export function addPptproject(data) {
  return request({
    url: '/project/pptproject',
    method: 'post',
    data: data
  })
}

// 修改项目管理
export function updatePptproject(data) {
  return request({
    url: '/project/pptproject',
    method: 'put',
    data: data
  })
}

// 删除项目管理
export function delPptproject(pptProjectId) {
  return request({
    url: '/project/pptproject/' + pptProjectId,
    method: 'delete'
  })
}
