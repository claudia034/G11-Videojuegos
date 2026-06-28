import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { GitBranch } from 'lucide-react'
import SectionBlock from '../components/SectionBlock'
import BracketBoard from '../components/BracketBoard'
import { tournamentService } from '../services/tournamentService'
import { storage } from '../services/api'

const formatDateTime = (value) => {
  if (!value) return 'Sin programar'
  return new Intl.DateTimeFormat('es-SV', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export default function Bracket(){
  const { id } = useParams()
  const [t, setT] = useState(null)
  const [bracket, setBracket] = useState(null)
  const currentUser = storage.getUser()
  const canManage = ['ADMIN', 'ORGANIZER'].includes(currentUser?.role)

  useEffect(()=>{
    tournamentService.getById(id).then(setT)
    tournamentService.getBracket(id).then(setBracket).catch(() => setBracket(null))
  },[id])

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
      </SectionBlock>
    </div>
  )
}
