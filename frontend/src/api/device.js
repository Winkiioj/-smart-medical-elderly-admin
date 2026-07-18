import request from '@/utils/request.js'

/**
 * 设备台账 API
 */

// 分页查询
export const getDevicePage = (params) => {
  return request({
    url: '/api/device/page',
    method: 'get',
    params,
  })
}

// 设备详情
export const getDeviceDetail = (id) => {
  return request({
    url: `/api/device/detail/${id}`,
    method: 'get',
  })
}

// 录入设备
export const addDevice = (data) => {
  return request({
    url: '/api/device/add',
    method: 'post',
    params: { operatorId: data.operatorId },
    data,
  })
}

// 编辑设备
export const updateDevice = (data) => {
  return request({
    url: '/api/device/update',
    method: 'put',
    data,
  })
}

// 更新设备状态
export const updateDeviceStatus = (id, status, operatorId) => {
  return request({
    url: `/api/device/status/${id}`,
    method: 'put',
    params: { status, operatorId },
  })
}

// 设备统计
export const getDeviceStats = () => {
  return request({
    url: '/api/device/stats',
    method: 'get',
  })
}

// 设备管理员 Dashboard
export const getDeviceDashboard = () => {
  return request({
    url: '/api/device/dashboard',
    method: 'get',
  })
}

// 医生提交设备报修
export const submitRepair = (data) => {
  return request({
    url: '/api/device/repair',
    method: 'post',
    data,
  })
}
