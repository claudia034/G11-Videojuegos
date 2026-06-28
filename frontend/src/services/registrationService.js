import { api } from './api'

const normalizeRegistration = (registration) => ({
  ...registration,
  tournamentId: registration.tournamentId,
  tournamentName: registration.tournamentName,
  participantId: registration.participantId,
  participantName: registration.participantName
})

export const registrationService = {
  listMine: async () => {
    const registrations = await api.get('/api/v1/registrations/me')
    return (registrations || []).map(normalizeRegistration)
  },
  registerPlayer: (tournamentId, playerId) =>
    api.post(`/api/v1/tournaments/${tournamentId}/register`, { playerId }),
  registerTeam: (tournamentId, teamId) =>
    api.post(`/api/v1/tournaments/${tournamentId}/register`, { teamId }),
  withdraw: (registrationId) => api.delete(`/api/v1/registrations/${registrationId}`)
}
