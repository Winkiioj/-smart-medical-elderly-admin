/**
 * 报表统计 API — A 负责
 */
import request from '@/utils/request.js'
import axios from 'axios'
import { getStorage } from '@/utils/localStorage.js'

const BASE = 'http://localhost:8080'

// 管理员 Dashboard
export const getDashboard = () => request({ url: '/api/reports/dashboard', method: 'get' })

// 全局总览
export const getOverview = () => request({ url: '/api/reports/overview', method: 'get' })

// 社区详情
export const getCommunityDetail = (community) =>
  request({ url: '/api/reports/community-detail', method: 'get', params: { community } })

// 导出全局总览 Excel（Blob 下载）
export const downloadOverviewExcel = async () => {
  const res = await axios.get(`${BASE}/api/reports/export/overview`, {
    responseType: 'blob',
    headers: { Token: getStorage('Token') || '' },
  })
  return res.data
}

// 导出社区详情 Excel（Blob 下载）
export const downloadCommunityDetailExcel = async (community) => {
  const res = await axios.get(`${BASE}/api/reports/export/community-detail`, {
    responseType: 'blob',
    headers: { Token: getStorage('Token') || '' },
    params: { community },
  })
  return res.data
}
