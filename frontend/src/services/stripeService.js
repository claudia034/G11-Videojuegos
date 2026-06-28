import { api } from './api'

export const stripeService = {
  checkoutTournament: (tournamentId) => api.post(`/api/v1/stripe/checkout-tournament/${tournamentId}`)
}
