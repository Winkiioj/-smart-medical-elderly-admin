/**
 * 老人档案 API — B 负责
 */
import request from '@/utils/request.js'

// 分页查询
export const getElderlyList = (params) => request({ url: '/api/elderly/list', method: 'get', params })

// 详情（含家属）
export const getElderlyDetail = (id) => request({ url: `/api/elderly/${id}`, method: 'get' })

// 新增
export const addElderly = (data) => request({ url: '/api/elderly', method: 'post', data })

// 编辑
export const updateElderly = (id, data) => request({ url: `/api/elderly/${id}`, method: 'put', data })

// 家属列表
export const getContacts = (elderlyId) => request({ url: `/api/elderly/${elderlyId}/contacts`, method: 'get' })
