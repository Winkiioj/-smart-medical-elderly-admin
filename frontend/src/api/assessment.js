import request from '@/utils/request.js'

export const getReportPage = (params) => {
  return request({ url: '/api/assessment/report/page', method: 'get', params })
}

export const createReport = (data) => {
  return request({ url: '/api/assessment/report/create', method: 'post', data })
}

export const saveScores = (reportId, scores) => {
  return request({ url: `/api/assessment/report/scores/${reportId}`, method: 'put', data: scores })
}

export const completeReport = (reportId, conclusion) => {
  return request({ url: `/api/assessment/report/complete/${reportId}`, method: 'post', data: { conclusion } })
}

export const getReportDetail = (id) => {
  return request({ url: `/api/assessment/report/detail/${id}`, method: 'get' })
}

export const getElderlyList = (doctorId) => {
  return request({ url: '/api/assessment/elderly-list', method: 'get', params: { doctorId } })
}

export const getTemplateList = () => {
  return request({ url: '/api/assessment/template/list', method: 'get' })
}
