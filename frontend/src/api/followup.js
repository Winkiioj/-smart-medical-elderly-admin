import request from '@/utils/request.js'

export const getFollowupPlanPage = (params) => {
  return request({ url: '/api/followup/plan/page', method: 'get', params })
}

export const createPlan = (data) => {
  return request({ url: '/api/followup/plan/create', method: 'post', data })
}

export const startPlan = (id, doctorId) => {
  return request({ url: `/api/followup/plan/start/${id}`, method: 'post', params: { doctorId } })
}

export const completePlan = (id, data) => {
  return request({ url: `/api/followup/plan/complete/${id}`, method: 'post', data })
}

export const updatePlanDate = (id, planDate) => {
  return request({ url: `/api/followup/plan/date/${id}`, method: 'put', params: { planDate } })
}

export const getRecentRecords = (elderlyId) => {
  return request({ url: `/api/followup/record/recent/${elderlyId}`, method: 'get' })
}

export const getRecordByPlan = (planId) => {
  return request({ url: `/api/followup/record/by-plan/${planId}`, method: 'get' })
}

export const getPlanDetail = (id) => {
  return request({ url: `/api/followup/plan/${id}`, method: 'get' })
}

export const getElderlyList = (doctorId) => {
  return request({ url: '/api/followup/elderly-list', method: 'get', params: { doctorId } })
}
