/**
 * 消息通知 API — A 负责
 */
import request from '@/utils/request.js'

// 通知列表
export const getNotifications = (pageNo, pageSize) =>
  request({ url: '/api/notification/page', method: 'get', params: { pageNo, pageSize } })

// 未读数量
export const getUnreadCount = () =>
  request({ url: '/api/notification/unread-count', method: 'get' })

// 标记已读
export const markRead = (id) =>
  request({ url: `/api/notification/read/${id}`, method: 'put' })

// 全部已读
export const markAllRead = () =>
  request({ url: '/api/notification/read-all', method: 'put' })
