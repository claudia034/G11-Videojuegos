import React from 'react'
import { Calendar, Swords } from 'lucide-react'

const formatScheduledAt = (value) => {
  if (!value) return 'Sin programar'

  return new Intl.DateTimeFormat('es-SV', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value))
}

export default function MatchCard({ match }){
  const roundLabel = match.round || match.roundName || 'Partida'
  const players = Array.isArray(match.players) && match.players.length
    ? match.players
    : [match.participant1Name, match.participant2Name].filter(Boolean)

  return (
    <article className="card interactive-card">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="eyebrow">{roundLabel}</div>
          <div className="mt-2 flex items-center gap-2 text-lg font-black">
            <Swords className="h-4 w-4 text-[#ff9f1c]" />
            <span className="truncate">{players.length ? players.join(' vs ') : 'Partida sin participantes'}</span>
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-3 text-xs font-medium text-slate-500">
            <span className="flex items-center gap-1.5">
              <Calendar className="h-3.5 w-3.5" />
              {formatScheduledAt(match.scheduledAt)}
            </span>
            {match.map && <span>Mapa: {match.map}</span>}
          </div>
        </div>
        <div className="pulse-badge rounded-md border border-[#38f8d4]/25 bg-[#38f8d4]/10 px-3 py-1 text-xs font-black uppercase text-[#38f8d4]">
          {match.score || match.status || 'Pendiente'}
        </div>
      </div>
    </article>
  )
}
