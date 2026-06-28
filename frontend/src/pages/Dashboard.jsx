import React, { useEffect, useState } from 'react'
import { Calendar, Shield, Trophy, User } from 'lucide-react'
import { tournamentService } from '../services/tournamentService'
import { playerService } from '../services/playerService'
import { storage } from '../services/api'
import { registrationService } from '../services/registrationService'
import StatCard from '../components/StatCard'
import TournamentCard from '../components/TournamentCard'
import MatchCard from '../components/MatchCard'
import SectionBlock from '../components/SectionBlock'

export default function Dashboard(){
  const [tournaments, setTournaments] = useState([])
  const [currentPlayer, setCurrentPlayer] = useState(null)
  const [matches, setMatches] = useState([])
  const [registrations, setRegistrations] = useState([])
  const currentUser = storage.getUser()
  const isAdmin = currentUser?.role === 'ADMIN'

  useEffect(() => {
    tournamentService.list().then(setTournaments).catch(() => setTournaments([]))

    if (!isAdmin) {
      playerService.getCurrent().then(setCurrentPlayer).catch(() => setCurrentPlayer(null))
      registrationService.listMine().then(setRegistrations).catch(() => setRegistrations([]))
    } else {
      setCurrentPlayer(null)
      setRegistrations([])
    }
  }, [isAdmin])

  useEffect(() => {
    if (isAdmin) {
      setMatches([])
      return
    }

    const currentRegistration = registrations.find((registration) =>
      ['CONFIRMED', 'PENDING'].includes(registration.status)
    )

    if (!currentRegistration?.tournamentId) {
      setMatches([])
      return
    }

    tournamentService.getMatches(currentRegistration.tournamentId).then(setMatches).catch(() => setMatches([]))
  }, [isAdmin, registrations])

  const activeTournaments = tournaments.filter((t) => ['IN_PROGRESS', 'REGISTRATION_OPEN', 'DRAFT'].includes(t.status))
  const nextMatch = matches[0]
  const playerInitials = currentPlayer?.name?.slice(0, 2).toUpperCase() || 'NP'
  const currentRegistration = registrations.find((registration) =>
    ['CONFIRMED', 'PENDING'].includes(registration.status)
  )

  return (
    <div className="space-y-8">
      <SectionBlock eyebrow="Lobby" title="Resumen jugador" icon={User}>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard title="Mi Elo" value={currentPlayer?.rating || 0} subtitle={currentPlayer?.rank || 'Sin datos'} accent="text-[#60e1cf]" />
          <StatCard title="Torneos Jugados" value={currentPlayer?.tournamentsPlayed || 0} subtitle="Datos de backend" accent="text-[#b65cff]" />
          <StatCard title="Victorias" value={currentPlayer?.wins || 0} subtitle={`${currentPlayer?.winRate || 0}% win rate`} accent="text-[#ff9f1c]" />
          <StatCard title="Derrotas" value={currentPlayer?.losses || 0} subtitle="Historial actual" accent="text-[#35d978]" />
        </div>
      </SectionBlock>

      <SectionBlock eyebrow="Competencia" title="Torneos activos" icon={Trophy}>
        <div className="grid gap-4 lg:grid-cols-3">
          {activeTournaments.map((t)=> <TournamentCard key={t.id} t={t} />)}
        </div>
      </SectionBlock>

      <SectionBlock
        eyebrow="Agenda"
        title="Actividad del jugador"
        icon={Calendar}
        action={currentRegistration ? <span className="w-fit rounded border border-emerald-400/25 bg-emerald-400/10 px-3 py-1 text-xs font-black uppercase text-emerald-300">{currentRegistration.tournamentName}</span> : null}
      >
        <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
          {nextMatch ? (
            <MatchCard match={nextMatch} />
          ) : (
            <article className="card">
              <div className="eyebrow">Partidas</div>
              <div className="mt-3 text-sm font-medium text-slate-400">
                {currentRegistration
                  ? 'Tu torneo actual todavia no tiene bracket o partidas visibles.'
                  : 'Aun no tienes inscripciones activas en torneos.'}
              </div>
            </article>
          )}

          <article className="card">
            <div className="eyebrow">Perfil activo</div>
            <div className="mt-4 flex items-center gap-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-[#b65cff] to-[#38f8d4] text-xl font-black text-black">
                {playerInitials}
              </div>
              <div>
                <div className="text-xl font-black">{currentPlayer?.name || currentUser?.email || 'Jugador'}</div>
                <div className="text-sm font-medium text-slate-500">
                  {isAdmin ? 'Administrador autenticado' : 'Jugador autenticado'}
                </div>
              </div>
            </div>
            <div className="mt-5 grid grid-cols-3 gap-3 text-center">
              <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                <div className="text-lg font-black text-[#38f8d4]">{currentPlayer?.tournamentsPlayed || 0}</div>
                <div className="text-[11px] font-bold uppercase text-slate-500">Torneos</div>
              </div>
              <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                <div className="text-lg font-black text-[#ff9f1c]">{currentPlayer?.wins || 0}</div>
                <div className="text-[11px] font-bold uppercase text-slate-500">Wins</div>
              </div>
              <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                <div className="text-lg font-black text-[#35d978]">{currentPlayer?.rating || 0}</div>
                <div className="text-[11px] font-bold uppercase text-slate-500">ELO</div>
              </div>
            </div>
          </article>
        </div>
      </SectionBlock>

      <SectionBlock
        eyebrow="Panel admin"
        title="Control de torneos"
        icon={Shield}
        action={isAdmin ? <span className="w-fit rounded border border-[#ff9f1c]/30 bg-[#ff9f1c]/10 px-3 py-1 text-xs font-black uppercase text-[#ffbf69]">Sesion admin</span> : null}
      >
        {isAdmin ? (
          <div className="grid gap-3 sm:grid-cols-4">
            {[
              ['Torneos visibles', tournaments.length],
              ['En curso', tournaments.filter((tournament) => tournament.status === 'IN_PROGRESS').length],
              ['Registro abierto', tournaments.filter((tournament) => tournament.status === 'REGISTRATION_OPEN').length],
              ['Borradores', tournaments.filter((tournament) => tournament.status === 'DRAFT').length]
            ].map(([label, value]) => (
              <div key={label} className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                <div className="text-2xl font-black text-white">{value}</div>
                <div className="mt-1 text-xs font-bold uppercase text-slate-500">{label}</div>
              </div>
            ))}
          </div>
        ) : (
          <article className="card">
            <div className="text-sm font-medium text-slate-400">
              Este panel solo se habilita para cuentas administrador.
            </div>
          </article>
        )}
      </SectionBlock>
    </div>
  )
}
