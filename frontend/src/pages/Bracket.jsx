import React from 'react'
import { Link, useParams } from 'react-router-dom'
import { GitBranch, Loader2 } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import SectionBlock from '../components/SectionBlock'
import BracketBoard from '../components/BracketBoard'
import { tournamentService } from '../services/tournamentService'
import { storage } from '../services/api'

const formatDateTime = (value) => {
  if (!value) return 'Sin programar'
  return new Intl.DateTimeFormat('es-SV', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export default function Bracket() {
  const { id } = useParams()
  const currentUser = storage.getUser()
  const canManage = ['ADMIN', 'ORGANIZER'].includes(currentUser?.role)

  const { data: t, isLoading: isLoadingTournament } = useQuery({
    queryKey: ['tournament', id],
    queryFn: () => tournamentService.getById(id)
  })

  const { data: bracket, isLoading: isLoadingBracket } = useQuery({
    queryKey: ['bracket', id],
    queryFn: () => tournamentService.getBracket(id),
    retry: false
  })

  const isLoading = isLoadingTournament || isLoadingBracket

  return (
    <div className="space-y-4">
      <SectionBlock
        eyebrow="Bracket"
        title={t?.name || 'Bracket del torneo'}
        icon={GitBranch}
        action={canManage ? (
          <Link to={`/tournaments/${id}`} className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69]">
            Gestionar torneo
          </Link>
        ) : null}
      >
        {isLoading ? (
          <div className="flex justify-center p-8">
            <Loader2 className="h-8 w-8 animate-spin text-[#b65cff]" />
          </div>
        ) : (
          <>
            {t && (
              <div className="mb-4 grid gap-3 md:grid-cols-4">
                <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                  <div className="text-xs font-bold uppercase text-slate-500">Estado</div>
                  <div className="mt-1 text-lg font-black text-white">{t.statusLabel}</div>
                </div>
                <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                  <div className="text-xs font-bold uppercase text-slate-500">Formato</div>
                  <div className="mt-1 text-lg font-black text-[#38f8d4]">{t.formatLabel}</div>
                </div>
                <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                  <div className="text-xs font-bold uppercase text-slate-500">Inicio</div>
                  <div className="mt-1 text-sm font-black text-[#ffbf69]">{formatDateTime(t.startAt)}</div>
                </div>
                <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                  <div className="text-xs font-bold uppercase text-slate-500">Capacidad</div>
                  <div className="mt-1 text-lg font-black text-[#b65cff]">{t.totalSlots}</div>
                </div>
              </div>
            )}
            <BracketBoard tournamentName={t?.name || 'Bracket del torneo'} rounds={bracket?.rounds || []} />
          </>
        )}
      </SectionBlock>
    </div>
  )
}
