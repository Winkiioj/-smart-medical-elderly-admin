import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getStorage } from '@/utils/localStorage.js'
import router from '@/router'

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  crossDomain: true,
})

// 请求拦截器：自动在请求头加 Token
service.interceptors.request.use(
  (config) => {
    const token = getStorage('Token')
    if (token) {
      config.headers['Token'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理返回结果
service.interceptors.response.use(
  (response) => {
    if (response.data) {
      if (response.data.code === 200) {
        return response.data
      } else if (response.data.code === 401) {
        ElMessage.error(response.data.msg || '登录已失效')
        router.push({ path: '/login' })
        return Promise.reject(new Error('未授权'))
      } else {
        ElMessage.error(response.data.msg || '操作失败')
        return Promise.reject(new Error(response.data.msg))
      }
    }
    return response
  },
  (error) => {
    ElMessage.error('网络异常，请检查网络连接')
    return Promise.reject(error)
  }
)

export default service
