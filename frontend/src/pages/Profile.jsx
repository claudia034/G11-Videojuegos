import React from 'react'
import { useParams } from 'react-router-dom'
import { User, Loader2 } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { playerService } from '../services/playerService'
import SectionBlock from '../components/SectionBlock'

export default function Profile() {
  const { id } = useParams()

  const { data: player, isLoading: isLoadingPlayer } = useQuery({
    queryKey: ['player', id],
    queryFn: () => (id === 'me' ? playerService.getCurrent() : playerService.getById(id)),
    retry: false
  })

  const { data: historyData, isLoading: isLoadingHistory } = useQuery({
    queryKey: ['playerHistory', player?.id],
    queryFn: () => playerService.getHistory(player.id),
    enabled: !!player?.id
  })

  const history = historyData?.matches || []
  const isLoading = isLoadingPlayer || isLoadingHistory

  return (
    <div className="space-y-4">
      <SectionBlock eyebrow="Perfil de usuario" title="Jugador" icon={User}>
        {isLoading ? (
          <div className="flex justify-center p-8">
            <Loader2 className="h-8 w-8 animate-spin text-[#b65cff]" />
          </div>
        ) : player ? (
          <div className="grid gap-4">
            <div className="grid gap-4 lg:grid-cols-[0.9fr_1.1fr]">
              <section className="card">
                <div className="flex items-center gap-4">
                  <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-[#b65cff] to-[#38f8d4] text-2xl font-black text-black">
                    {player.name.slice(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <div className="text-2xl font-black">{player.name}</div>
                    <div className="text-sm font-medium text-slate-500">Perfil sincronizado con backend</div>
                    <div className="mt-2 rounded-full border border-[#38f8d4]/25 bg-[#38f8d4]/10 px-3 py-1 text-xs font-black uppercase text-[#38f8d4]">
                      {player.rank}
                    </div>
                  </div>
                </div>
              </section>

              <section className="card">
                <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
                  <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                    <div className="eyebrow">ELO</div>
                    <div className="mt-2 break-words text-3xl font-black leading-none text-[#38f8d4]">{player.rating}</div>
                  </div>
                  <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                    <div className="eyebrow">Torneos</div>
                    <div className="mt-2 break-words text-3xl font-black leading-none text-[#b65cff]">{player.tournamentsPlayed}</div>
                  </div>
                  <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                    <div className="eyebrow">Victorias</div>
                    <div className="mt-2 break-words text-3xl font-black leading-none text-[#ff9f1c]">{player.wins}</div>
                  </div>
                  <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                    <div className="eyebrow">Win rate</div>
                    <div className="mt-2 break-words text-2xl font-black leading-none text-[#35d978]">{player.winRate}%</div>
                  </div>
                  <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                    <div className="eyebrow">Puntos</div>
                    <div className="mt-2 break-words text-3xl font-black leading-none text-[#ff9f1c]">{player.virtualPoints}</div>
                  </div>
                </div>
              </section>
            </div>

            <section className="card">
              <div className="eyebrow">Historial de torneos y partidas</div>
              <div className="mt-4 grid gap-3">
                {history.length ? history.map((match) => (
                  <article key={match.matchId} className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <div className="font-black">{match.tournamentName}</div>
                        <div className="text-sm font-medium text-slate-400">vs {match.opponentName}</div>
                      </div>
                      <div className={`text-lg font-black ${match.won ? 'text-[#35d978]' : 'text-[#ff9f1c]'}`}>
                        {match.myScore} - {match.opponentScore}
                      </div>
                    </div>
                    <div className="mt-2 text-xs font-bold uppercase text-slate-500">
                      {match.won ? 'Victoria' : 'Derrota'} • {new Date(match.completedAt).toLocaleString('es-SV')}
                    </div>
                  </article>
                )) : (
                  <div className="text-sm font-medium text-slate-400">Todavia no hay historial para este jugador.</div>
                )}
              </div>
            </section>
          </div>
        ) : (
          <div className="card">No se pudo cargar el perfil del jugador.</div>
        )}
      </SectionBlock>
    </div>
  )
}
