import { api } from './api'

const statusLabelMap = {
  DRAFT: 'Borrador',
  REGISTRATION_OPEN: 'Registro abierto',
  REGISTRATION_CLOSED: 'Registro cerrado',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completado',
  CANCELLED: 'Cancelado'
}

const formatLabelMap = {
  SINGLE_ELIMINATION: 'Eliminacion simple',
  DOUBLE_ELIMINATION: 'Doble eliminacion',
  ROUND_ROBIN: 'Round robin',
  SWISS: 'Swiss'
}

const progressByStatus = {
  DRAFT: 12,
  REGISTRATION_OPEN: 35,
  REGISTRATION_CLOSED: 55,
  IN_PROGRESS: 78,
  COMPLETED: 100,
  CANCELLED: 0
}

const normalizeTournament = (tournament) => ({
  ...tournament,
  game: tournament.gameName,
  participants: tournament.maxParticipants,
  totalSlots: tournament.maxParticipants,
  currentParticipants: tournament.currentParticipants ?? tournament.participantCount ?? 0,
  prize: (tournament.prizes || []).reduce((sum, prize) => sum + Number(prize.amount || 0), 0),
  status: tournament.status,
  statusLabel: statusLabelMap[tournament.status] || tournament.status,
  formatLabel: tournament.formatDisplayName || formatLabelMap[tournament.format] || tournament.format,
  format: tournament.format,
  progress: progressByStatus[tournament.status] ?? 0,
  cta: tournament.status === 'IN_PROGRESS' ? 'Ver bracket' : 'Ver detalle'
})

export const tournamentService = {
  list: async () => {
    const tournaments = await api.get('/api/v1/tournaments')
    return (tournaments || []).map(normalizeTournament)
  },
  getById: async (id) => {
    const tournament = await api.get(`/api/v1/tournaments/${id}`)
    return normalizeTournament(tournament)
  },
  getMatches: async (tournamentId, status) => {
    const suffix = status ? `?status=${status}` : ''
    return api.get(`/api/v1/tournaments/${tournamentId}/matches${suffix}`)
  },
  getManagement: async (tournamentId) =>
    api.get(`/api/v1/tournaments/${tournamentId}/management`),
  getParticipants: async (tournamentId) => {
    const page = await api.get(`/api/v1/tournaments/${tournamentId}/participants`)
    return page?.content || []
  },
  getBracket: (tournamentId) => api.get(`/api/v1/tournaments/${tournamentId}/brackets`),
  publish: (tournamentId) => api.post(`/api/v1/tournaments/${tournamentId}/publish`),
  closeRegistration: (tournamentId) => api.post(`/api/v1/tournaments/${tournamentId}/close-registration`),
  generateRounds: (tournamentId) => api.post(`/api/v1/tournaments/${tournamentId}/rounds`),
  generateBracket: (tournamentId, generationType = 'BY_RANKING') =>
    api.post(`/api/v1/tournaments/${tournamentId}/brackets/generate`, {
      generationType,
      defaultBestOf: 1
    }),
  scheduleRound: (roundId, scheduledStart) =>
    api.patch(`/api/v1/rounds/${roundId}/schedule`, { scheduledStart }),
  create: async (payload) => {
    const tournament = await api.post('/api/v1/tournaments', payload)
    return normalizeTournament(tournament)
  }
}
