import request from '@/utils/request.js'

export const getWarningPage = (params) => {
  return request({ url: '/api/warning/page', method: 'get', params })
}

export const getWarningDetail = (id) => {
  return request({ url: `/api/warning/detail/${id}`, method: 'get' })
}

export const acceptWarning = (id, handlerId) => {
  return request({ url: `/api/warning/accept/${id}`, method: 'post', params: { handlerId } })
}

export const completeWarning = (id, opinion, result) => {
  return request({ url: `/api/warning/complete/${id}`, method: 'post', params: { opinion, result } })
}

export const closeWarning = (id, reason) => {
  return request({ url: `/api/warning/close/${id}`, method: 'post', params: { reason } })
}
