import { api } from './api'

const normalizePlayer = (player) => ({
  id: player.playerId,
  name: player.username,
  rating: player.eloRating,
  wins: player.wins,
  losses: player.losses,
  tournamentsPlayed: player.tournamentsPlayed,
  virtualPoints: player.virtualPoints ?? 0,
  winRate: Number((player.winRate ?? ((player.wins / Math.max(player.wins + player.losses, 1)) * 100)).toFixed(1)),
  rank: `Top ELO ${player.eloRating}`
})

export const playerService = {
  list: async () => {
    const page = await api.get('/api/v1/players/ranking')
    return (page?.content || []).map(normalizePlayer)
  },
  getCurrent: async () => {
    const stats = await api.get('/api/v1/players/me/stats')
    return normalizePlayer(stats)
  },
  getById: async (id) => {
    const stats = await api.get(`/api/v1/players/${id}/stats`)
    return normalizePlayer(stats)
  },
  getHistory: async (id, filters = {}) => {
    const params = new URLSearchParams()
    if (filters.gameName) params.set('gameName', filters.gameName)
    if (filters.tournamentName) params.set('tournamentName', filters.tournamentName)
    const suffix = params.toString() ? `?${params.toString()}` : ''
    return api.get(`/api/v1/players/${id}/history${suffix}`)
  }
}
