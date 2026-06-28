import React, { useEffect, useState } from 'react'
import { Trophy } from 'lucide-react'
import { tournamentService } from '../services/tournamentService'
import { registrationService } from '../services/registrationService'
import { playerService } from '../services/playerService'
import { storage } from '../services/api'
import TournamentCard from '../components/TournamentCard'
import SectionBlock from '../components/SectionBlock'

export default function Tournaments(){
  const [list, setList] = useState([])
  const [registrations, setRegistrations] = useState([])
  const [currentPlayer, setCurrentPlayer] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const currentUser = storage.getUser()
  const isPlayer = currentUser?.role === 'PLAYER'

  useEffect(() => {
    tournamentService.list().then(setList)

    if (isPlayer) {
      playerService.getCurrent().then(setCurrentPlayer).catch(() => setCurrentPlayer(null))
      registrationService.listMine().then(setRegistrations).catch(() => setRegistrations([]))
    }
  }, [isPlayer])

  const register = async (tournamentId) => {
    if (!currentPlayer?.id) {
      setError('No se pudo identificar tu perfil de jugador.')
      return
    }

    setMessage('')
    setError('')

    try {
      await registrationService.registerPlayer(tournamentId, currentPlayer.id)
      setMessage('Inscripcion realizada exitosamente.')
      const updated = await registrationService.listMine()
      setRegistrations(updated)
    } catch (err) {
      setError(err.message || 'No se pudo completar la inscripcion.')
    }
  }

  const registeredTournamentIds = new Set(registrations.map((registration) => registration.tournamentId))

  return (
    <div className="space-y-4">
      <SectionBlock eyebrow="Lobby" title="Torneos disponibles" icon={Trophy}>
        {message && <div className="rounded-md border border-emerald-400/30 bg-emerald-500/10 px-3 py-2 text-sm font-bold text-emerald-300">{message}</div>}
        {error && <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">{error}</div>}

        <div className="grid gap-4 md:grid-cols-3">
          {list.map((tournament) => {
            const isRegistered = registeredTournamentIds.has(tournament.id)
            const canRegister = isPlayer && tournament.status === 'REGISTRATION_OPEN' && !isRegistered

            return (
              <div key={tournament.id} className="space-y-3">
                <TournamentCard t={tournament} />
                {isPlayer && (
                  <button
                    type="button"
                    disabled={!canRegister}
                    onClick={() => register(tournament.id)}
                    className="w-full rounded-md border border-[#2d1747] bg-black/20 px-4 py-3 text-sm font-black text-slate-200 transition-colors hover:border-[#38f8d4] hover:text-[#38f8d4] disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {isRegistered ? 'Ya inscrito' : canRegister ? 'Inscribirme' : 'Inscripcion no disponible'}
                  </button>
                )}
              </div>
            )
          })}
        </div>
      </SectionBlock>
    </div>
  )
}
