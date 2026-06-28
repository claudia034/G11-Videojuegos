import { api, storage } from './api'

const accentByTitle = (title = '') => {
  const normalized = title.toLowerCase()

  if (normalized.includes('bracket') || normalized.includes('torneo')) return 'text-[#b65cff]'
  if (normalized.includes('admin') || normalized.includes('revision')) return 'text-[#ff9f1c]'
  return 'text-[#38f8d4]'
}

export const notificationService = {
  listUnread: async () => {
    const notifications = await api.get('/api/notifications')
    return (notifications || []).map((notification) => ({
      ...notification,
      accent: accentByTitle(notification.title)
    }))
  },
  markAsRead: (id) => api.put(`/api/notifications/${id}/read`),
  stream: () => {
    const token = storage.getToken()
    const suffix = token ? `?access_token=${encodeURIComponent(token)}` : ''
    return new EventSource(`http://localhost:8081/api/notifications/stream${suffix}`, { withCredentials: false })
  }
}
