/**
 * 社区管理 API — A 负责
 */
import request from '@/utils/request.js'

export const getCommunityPage = (pageNo, pageSize, keyword) =>
  request({ url: '/api/community/page', method: 'get', params: { pageNo, pageSize, keyword } })

export const getEnabledCommunities = () =>
  request({ url: '/api/community/enabled', method: 'get' })

export const addCommunity = (data) =>
  request({ url: '/api/community', method: 'post', data })

export const updateCommunity = (data) =>
  request({ url: '/api/community', method: 'put', data })

export const toggleCommunityStatus = (id, status) =>
  request({ url: '/api/community/toggle-status', method: 'put', data: { id, status } })
