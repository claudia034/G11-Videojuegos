import { api } from './api'

export const matchService = {
  getById: (matchId) => api.get(`/api/v1/matches/${matchId}`),
  schedule: (matchId, scheduledAt) => api.patch(`/api/v1/matches/${matchId}/schedule`, { scheduledAt }),
  adminDecision: (matchId, payload) => api.post(`/api/v1/matches/${matchId}/admin-decision`, payload),
  start: (matchId) => api.post(`/api/v1/matches/${matchId}/start`),
  submitResult: (matchId, payload) => api.post(`/api/v1/matches/${matchId}/result`, payload),
  confirmResult: (matchId) => api.post(`/api/v1/matches/${matchId}/confirm`),
  disputeResult: (matchId, reason) =>
    api.post(`/api/v1/matches/${matchId}/dispute?reason=${encodeURIComponent(reason)}`),
  resolveDispute: (matchId, adminUserId, payload) =>
    api.put(`/api/v1/disputes/${matchId}/resolve?adminUserId=${adminUserId}`, payload)
}
