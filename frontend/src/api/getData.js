/**
 * API 请求封装
 *
 * 各业务模块 API 按以下规范分文件编写：
 *   src/api/auth.js      — A 负责：登录/注册/Token
 *   src/api/elderly.js   — B 负责：老人档案
 *   src/api/health.js    — B 负责：健康数据
 *   src/api/warning.js   — C 负责：预警管理
 *   src/api/followup.js  — C 负责：随访管理
 *   src/api/assessment.js— C 负责：评估报告
 *   src/api/device.js    — C 负责：设备管理
 *   src/api/report.js    — A 负责：报表统计
 */
import request from '@/utils/request.js'

// ===== 示例 API =====
export const getHealthCheck = () => {
  return request({
    url: '/api/health',
    method: 'get',
  })
}
