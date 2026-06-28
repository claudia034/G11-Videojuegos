import axios from 'axios'

const API_BASE_URL = 'http://localhost:8081'

export const storage = {
  getToken: () => localStorage.getItem('nexus-token'),
  getRefreshToken: () => localStorage.getItem('nexus-refresh-token'),
  setSession: (session) => {
    localStorage.setItem('nexus-token', session.accessToken)
    localStorage.setItem('nexus-refresh-token', session.refreshToken)
    localStorage.setItem('nexus-role', session.user.role.toLowerCase())
    localStorage.setItem('nexus-user', session.user.email)
    localStorage.setItem('nexus-user-data', JSON.stringify(session.user))
  },
  clearSession: () => {
    localStorage.removeItem('nexus-token')
    localStorage.removeItem('nexus-refresh-token')
    localStorage.removeItem('nexus-role')
    localStorage.removeItem('nexus-user')
    localStorage.removeItem('nexus-user-data')
  },
  getUser: () => {
    const raw = localStorage.getItem('nexus-user-data')
    if (!raw) return null

    try {
      return JSON.parse(raw)
    } catch {
      localStorage.removeItem('nexus-user-data')
      return null
    }
  }
}

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

axiosInstance.interceptors.request.use(
  (config) => {
    const token = storage.getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

axiosInstance.interceptors.response.use(
  (response) => {
    // If backend wraps response in 'data' field (ApiResponse pattern)
    if (response.data && response.data.hasOwnProperty('data')) {
      return response.data.data
    }
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // Logic for refresh token could go here.
      // For now, clear session and force login if unauthorized
      storage.clearSession()
      window.location.href = '/login'
    }
    
    const message = error.response?.data?.message || 'No se pudo completar la solicitud.'
    return Promise.reject(new Error(message))
  }
)

export const api = {
  get: (path, config) => axiosInstance.get(path, config),
  post: (path, body, config) => axiosInstance.post(path, body, config),
  put: (path, body, config) => axiosInstance.put(path, body, config),
  patch: (path, body, config) => axiosInstance.patch(path, body, config),
  delete: (path, config) => axiosInstance.delete(path, config)
}
