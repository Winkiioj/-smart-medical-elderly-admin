/**
 * 健康数据 API — B 负责
 */
import request from '@/utils/request.js'

// 查某老人健康数据
export const getHealthList = (elderlyId, params) =>
  request({ url: `/api/health-record/list/${elderlyId}`, method: 'get', params })

// 批量导入
export const importHealthData = (data) =>
  request({ url: '/api/health-record/import', method: 'post', data })

// 下载模板
export const getTemplate = (doctorId) =>
  request({ url: `/api/health-record/template?doctorId=${doctorId}`, method: 'get' })

// 趋势图
export const getTrend = (params) =>
  request({ url: '/api/health-record/trend', method: 'get', params })

// Dashboard
export const getDashboard = (doctorId) =>
  request({ url: `/api/doctor/dashboard?doctorId=${doctorId}`, method: 'get' })
