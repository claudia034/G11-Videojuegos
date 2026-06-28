import { api } from './api'

export const reportService = {
  popularGames: async () => {
    const reports = await api.get('/api/reports/popular-games')
    return (reports || []).map((item) => ({
      ...item,
      totalTournaments: item.totalTournaments ?? item.tournamentCount ?? 0
    }))
  },
  popularTournamentsByGame: async () => {
    const reports = await api.get('/api/reports/popular-tournaments-by-game')
    return (reports || []).map((item) => ({
      ...item,
      participantCount: Number(item.participantCount || 0),
      capacity: Number(item.capacity || 0),
      occupancyRate: item.capacity ? Math.round((Number(item.participantCount || 0) / Number(item.capacity)) * 100) : 0
    }))
  },
}
