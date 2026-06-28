import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Eye, RefreshCw } from 'lucide-react'
import SectionBlock from '../components/SectionBlock'
import BracketBoard from '../components/BracketBoard'
import { tournamentService } from '../services/tournamentService'

export default function Spectator() {
  const { id } = useParams()
  const [tournament, setTournament] = useState(null)
  const [bracket, setBracket] = useState(null)
  const [updatedAt, setUpdatedAt] = useState(null)
  const [error, setError] = useState('')
  const [autoRefresh, setAutoRefresh] = useState(true)
  const [refreshNonce, setRefreshNonce] = useState(0)

  useEffect(() => {
    let cancelled = false

    const load = async () => {
      try {
        const [tournamentData, bracketData] = await Promise.all([
          tournamentService.getById(id),
          tournamentService.getBracket(id).catch(() => null)
        ])

        if (cancelled) return

        setTournament(tournamentData)
        setBracket(bracketData)
        setUpdatedAt(new Date())
        setError('')
      } catch (err) {
        if (cancelled) return
        setError(err.message || 'No se pudo cargar el modo espectador.')
      }
    }

    load()
    const timer = autoRefresh ? window.setInterval(load, 15000) : null

    return () => {
      cancelled = true
      if (timer) window.clearInterval(timer)
    }
  }, [id, autoRefresh, refreshNonce])

  const totalMatches = (bracket?.rounds || []).reduce((sum, round) => sum + (round.matches?.length || 0), 0)
  const completedMatches = (bracket?.rounds || []).reduce((sum, round) => (
    sum + (round.matches || []).filter((match) => match.status === 'COMPLETED').length
  ), 0)
  const liveMatches = (bracket?.rounds || []).reduce((sum, round) => (
    sum + (round.matches || []).filter((match) => match.status === 'IN_PROGRESS').length
  ), 0)
  const currentLeader = [...(bracket?.rounds || [])]
    .reverse()
    .flatMap((round) => round.matches || [])
    .find((match) => match.winnerName)?.winnerName

  return (
    <div className="space-y-4">
      <SectionBlock
        eyebrow="Live"
        title={tournament?.name || 'Modo espectador'}
        icon={Eye}
        action={
          <div className="flex flex-wrap gap-2">
            <span className="rounded-md border border-[#2d1747] bg-black/20 px-3 py-2 text-xs font-black uppercase text-slate-300">
              {autoRefresh ? 'Actualiza cada 15s' : 'Actualizacion manual'}
            </span>
            <button
              type="button"
              onClick={() => setAutoRefresh((current) => !current)}
              className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69]"
            >
              {autoRefresh ? 'Pausar auto-refresh' : 'Activar auto-refresh'}
            </button>
            <button
              type="button"
              onClick={() => setRefreshNonce((current) => current + 1)}
              className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]"
            >
              Refrescar panel
            </button>
            <Link to={`/tournaments/${id}`} className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]">
              Ver detalle
            </Link>
          </div>
        }
      >
        {error && (
          <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">
            {error}
          </div>
        )}

        <div className="card">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="text-lg font-black">{tournament?.game || 'Bracket en vivo'}</div>
              <div className="text-sm font-medium text-slate-400">
                {tournament?.formatLabel || tournament?.format || 'Formato en definicion'}
              </div>
            </div>
            <div className="flex items-center gap-2 text-xs font-bold uppercase text-slate-500">
              <RefreshCw className="h-4 w-4" />
              {updatedAt ? `Ultima lectura ${updatedAt.toLocaleTimeString('es-SV')}` : 'Cargando'}
            </div>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-4">
          <div className="card">
            <div className="eyebrow">Estado</div>
            <div className="mt-2 text-2xl font-black text-white">{tournament?.statusLabel || 'Cargando'}</div>
            <div className="text-xs font-bold uppercase text-slate-500">Estado del torneo</div>
          </div>
          <div className="card">
            <div className="eyebrow">Partidas</div>
            <div className="mt-2 text-2xl font-black text-[#38f8d4]">{completedMatches}/{totalMatches}</div>
            <div className="text-xs font-bold uppercase text-slate-500">Cerradas</div>
          </div>
          <div className="card">
            <div className="eyebrow">En vivo</div>
            <div className="mt-2 text-2xl font-black text-[#ff9f1c]">{liveMatches}</div>
            <div className="text-xs font-bold uppercase text-slate-500">Partidas jugando</div>
          </div>
          <div className="card">
            <div className="eyebrow">Lider actual</div>
            <div className="mt-2 text-lg font-black text-[#b65cff]">{currentLeader || 'Sin definir'}</div>
            <div className="text-xs font-bold uppercase text-slate-500">Ultimo ganador visible</div>
          </div>
        </div>

        <BracketBoard
          tournamentName={tournament?.name || 'Bracket'}
          rounds={bracket?.rounds || []}
          readonly
        />
      </SectionBlock>
    </div>
  )
}
