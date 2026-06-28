import { api, storage } from './api'

export const authService = {
  async login(email, password) {
    const session = await api.post('/api/v1/auth/login', { email, password })
    storage.setSession(session)
    return session
  },
  async register({ email, username, password, confirmPassword, role }) {
    const session = await api.post('/api/v1/auth/register', {
      email,
      username,
      password,
      confirmPassword,
      role
    })
    storage.setSession(session)
    return session
  },
  async me() {
    return api.get('/api/v1/auth/me')
  },
  logout() {
    storage.clearSession()
  }
}
