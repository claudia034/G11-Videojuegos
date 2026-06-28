import React, { useEffect, useState } from 'react'
import { BarChart3, Trophy } from 'lucide-react'
import SectionBlock from '../components/SectionBlock'
import { reportService } from '../services/reportService'
import { tournamentService } from '../services/tournamentService'

export default function Reports() {
  const [popularGames, setPopularGames] = useState([])
  const [popularTournamentsByGame, setPopularTournamentsByGame] = useState([])
  const [tournaments, setTournaments] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      reportService.popularGames(),
      reportService.popularTournamentsByGame().catch(() => []),
      tournamentService.list().catch(() => [])
    ])
      .then(([games, leaders, list]) => {
        setPopularGames(games)
        setPopularTournamentsByGame(leaders)
        setTournaments(list)
      })
      .catch((err) => setError(err.message || 'No se pudieron cargar los reportes.'))
  }, [])

  const totalTournaments = tournaments.length
  const activeTournaments = tournaments.filter((tournament) => tournament.status === 'IN_PROGRESS').length
  const uniqueGames = new Set(tournaments.map((tournament) => tournament.game)).size
  const tournamentsByGame = tournaments.reduce((acc, tournament) => {
    const key = tournament.game || 'Sin juego'
    acc[key] = acc[key] || []
    acc[key].push(tournament)
    return acc
  }, {})

  const popularityStories = popularTournamentsByGame.map((entry, index) => {
    const related = tournamentsByGame[entry.gameName] || []
    const second = [...related]
      .filter((candidate) => candidate.id !== entry.tournamentId)
      .sort((a, b) => {
        const participantGap = (b.currentParticipants || 0) - (a.currentParticipants || 0)
        return participantGap !== 0 ? participantGap : (b.totalSlots || 0) - (a.totalSlots || 0)
      })[0]
    const lead = second ? entry.participantCount - (second.currentParticipants || 0) : entry.participantCount
    return {
      ...entry,
      rank: index + 1,
      gameTournamentCount: related.length,
      lead
    }
  })

  return (
    <div className="space-y-6">
      <SectionBlock eyebrow="Analytics" title="Reportes de torneos" icon={BarChart3}>
        {error && (
          <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">
            {error}
          </div>
        )}

        <div className="mb-3 grid gap-6 md:mb-5 md:gap-8 md:grid-cols-3">
          <article className="card min-h-[136px] p-6 md:p-7">
            <div className="eyebrow">Cobertura</div>
            <div className="mt-3 text-4xl font-black leading-none text-[#b65cff]">{totalTournaments}</div>
            <div className="mt-2 text-xs font-bold uppercase text-slate-500">Torneos registrados</div>
          </article>

          <article className="card min-h-[136px] p-6 md:p-7">
            <div className="eyebrow">En curso</div>
            <div className="mt-3 text-4xl font-black leading-none text-[#38f8d4]">{activeTournaments}</div>
            <div className="mt-2 text-xs font-bold uppercase text-slate-500">Torneos activos</div>
          </article>

          <article className="card min-h-[136px] p-6 md:p-7">
            <div className="eyebrow">Variedad</div>
            <div className="mt-3 text-4xl font-black leading-none text-[#ff9f1c]">{uniqueGames}</div>
            <div className="mt-2 text-xs font-bold uppercase text-slate-500">Juegos con actividad</div>
          </article>
        </div>

        <div className="grid gap-6 lg:grid-cols-[1.04fr_0.96fr]">
          <section className="card p-6">
            <div className="eyebrow">Torneos mas populares por juego</div>
            <div className="mt-2 text-sm font-medium text-slate-400">
              Este bloque te dice, por cada juego, cual es el torneo que mejor esta funcionando y que tan lleno esta respecto a su capacidad.
            </div>
            <div className="mt-6 grid gap-5">
              {popularityStories.length ? popularityStories.map((entry) => (
                <article key={`${entry.gameName}-${entry.tournamentId}`} className="rounded-2xl border border-[#2d1747] bg-black/20 p-6">
                  <div className="flex flex-wrap items-start justify-between gap-6">
                    <div className="min-w-0 flex-1">
                      <div className="text-lg font-black text-white">{entry.gameName}</div>
                      <div className="text-xs font-bold uppercase text-slate-500">
                        Ranking #{entry.rank} • {entry.gameTournamentCount} torneo{entry.gameTournamentCount === 1 ? '' : 's'} en este juego
                      </div>
                      <div className="mt-2 text-sm font-medium text-slate-400">
                        Lider actual: <span className="font-black text-slate-200">{entry.tournamentName}</span>
                      </div>
                      <div className="mt-2 text-sm font-medium text-slate-500">
                        Ventaja de popularidad: <span className="font-black text-white">{entry.lead}</span> inscripcion{entry.lead === 1 ? '' : 'es'} sobre el siguiente referente.
                      </div>
                    </div>
                    <div className="min-w-[152px] rounded-xl border border-[#2d1747] bg-black/20 p-4 text-right">
                      <div className="text-3xl font-black leading-none text-[#38f8d4]">{entry.participantCount}</div>
                      <div className="text-xs font-bold uppercase text-slate-500">Inscritos</div>
                      <div className="mt-1 text-[11px] font-bold uppercase text-slate-600">
                        Cupo {entry.capacity} • {entry.status}
                      </div>
                      <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-[#120f1d]">
                        <div className="progress-bar h-full rounded-full" style={{ width: `${Math.min(entry.occupancyRate, 100)}%` }} />
                      </div>
                      <div className="mt-1 text-[11px] font-bold uppercase text-slate-500">
                        {entry.occupancyRate}% de ocupacion
                      </div>
                    </div>
                  </div>
                </article>
              )) : (
                <div className="text-sm font-medium text-slate-400">
                  Todavia no hay suficiente informacion para el ranking de popularidad por torneo.
                </div>
              )}
            </div>
          </section>

          <section className="card p-6">
            <div className="eyebrow">Lectura rapida</div>
            <div className="mt-6 grid gap-5">
              {popularGames.slice(0, 4).map((game, index) => (
                <div key={game.gameName} className="rounded-2xl border border-[#2d1747] bg-black/20 p-5">
                  <div className="flex items-center gap-4">
                    <div className="rounded-lg border border-[#2d1747] bg-black/20 p-2 text-[#ff9f1c]">
                      <Trophy className="h-4 w-4" />
                    </div>
                    <div>
                      <div className="font-black">#{index + 1} {game.gameName}</div>
                      <div className="text-sm font-medium text-slate-400">
                        {game.totalTournaments} torneos organizados
                      </div>
                      <div className="text-xs font-bold uppercase text-slate-500">
                        {Math.round((game.totalTournaments / Math.max(totalTournaments, 1)) * 100)}% del catalogo actual
                      </div>
                    </div>
                  </div>
                </div>
              ))}
              {!popularGames.length && (
                <div className="text-sm font-medium text-slate-400">
                  Cuando haya torneos creados, aqui veras tendencias por juego.
                </div>
              )}
            </div>
          </section>
        </div>
      </SectionBlock>
    </div>
  )
}
