import request from '@/utils/request.js'

/**
 * 老人标签 API
 */

export const getTagList = () => {
  return request({ url: '/api/tag/list', method: 'get' })
}

export const addTag = (data) => {
  return request({ url: '/api/tag', method: 'post', data })
}

export const updateTag = (data) => {
  return request({ url: '/api/tag', method: 'put', data })
}

export const deleteTag = (id) => {
  return request({ url: `/api/tag/${id}`, method: 'delete' })
}

export const getTagsByElderly = (elderlyId) => {
  return request({ url: `/api/tag/elderly/${elderlyId}`, method: 'get' })
}

export const saveTagsByElderly = (elderlyId, tagIds) => {
  return request({ url: `/api/tag/elderly/${elderlyId}`, method: 'put', data: tagIds })
}
