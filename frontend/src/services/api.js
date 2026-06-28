const API_BASE_URL = 'http://localhost:8081'

export const storage = {
  getToken: () => localStorage.getItem('nexus-token'),
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

async function request(path, options = {}) {
  const headers = new Headers(options.headers || {})
  headers.set('Content-Type', 'application/json')

  const token = storage.getToken()
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers
    })
  } catch (error) {
    throw new Error('No se pudo conectar con el backend en http://localhost:8081. Verifica que el servidor esté levantado.')
  }

  if (response.status === 204) {
    return null
  }

  const payload = await response.json().catch(() => null)
  const data = payload && Object.prototype.hasOwnProperty.call(payload, 'data')
    ? payload.data
    : payload

  if (!response.ok) {
    const message = payload?.message || 'No se pudo completar la solicitud.'
    throw new Error(message)
  }

  return data
}

export const api = {
  get: (path) => request(path),
  post: (path, body) =>
    request(path, {
      method: 'POST',
      body: JSON.stringify(body)
    }),
  put: (path, body) =>
    request(path, {
      method: 'PUT',
      body: body == null ? undefined : JSON.stringify(body)
    }),
  patch: (path, body) =>
    request(path, {
      method: 'PATCH',
      body: body == null ? undefined : JSON.stringify(body)
    }),
  delete: (path) =>
    request(path, {
      method: 'DELETE'
    })
}
