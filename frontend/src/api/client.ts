import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

// Create axios instance with common config
const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add auth token
    const token = localStorage.getItem('auth_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response.data,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      router.push('/login')
      ElMessage.error('Authentication expired. Please login again.')
    } else if (error.response?.status === 403) {
      ElMessage.error('You do not have permission to perform this action.')
    } else if (error.response?.status === 404) {
      ElMessage.error('Resource not found.')
    } else if (error.response?.status >= 500) {
      ElMessage.error('Server error. Please try again later.')
    } else if (!error.response) {
      ElMessage.error('Network error. Please check your connection.')
    }
    return Promise.reject(error)
  }
)

export default apiClient