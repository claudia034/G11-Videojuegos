import { api } from './api'

export const tournamentFormatService = {
  list: async () => {
    const formats = await api.get('/api/v1/tournament-formats')
    return formats || []
  }
}
