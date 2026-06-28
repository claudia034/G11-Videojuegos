import React from 'react'
import { DollarSign, GitBranch, Users } from 'lucide-react'
import { Link } from 'react-router-dom'

const statusStyles = {
  IN_PROGRESS: 'border-emerald-500/30 bg-emerald-500/15 text-emerald-300',
  REGISTRATION_OPEN: 'border-cyan-400/30 bg-cyan-400/15 text-cyan-300',
  DRAFT: 'border-[#ff9f1c]/35 bg-[#ff9f1c]/15 text-[#ffbf69]',
  COMPLETED: 'border-slate-600 bg-slate-700/30 text-slate-300'
}

const actionTo = (t) => t.status === 'IN_PROGRESS' ? `/bracket/${t.id}` : `/tournaments/${t.id}`

export default function TournamentCard({ t }) {
  return (
    <article className="card interactive-card flex min-h-[226px] flex-col justify-between overflow-hidden p-0">
      <div className="p-4">
        <div className="mb-3 flex items-center justify-between gap-3">
          <div className="eyebrow">{t.game}</div>
          <span className={`rounded px-2.5 py-1 text-[10px] font-black uppercase tracking-wide ${statusStyles[t.status] || statusStyles.finished}`}>
            {t.statusLabel || t.status}
          </span>
        </div>

        <h3 className="max-w-[15rem] text-xl font-black leading-5 text-slate-100">{t.name}</h3>

        <div className="mt-5 grid gap-3 text-xs font-medium text-slate-500">
          <div className="flex items-center gap-2">
            <Users className="h-3.5 w-3.5" />
            <span>{t.participants} jugadores</span>
            <GitBranch className="ml-2 h-3.5 w-3.5" />
            <span>{t.formatLabel || t.format}</span>
          </div>
          <div className="flex items-center gap-2">
            <DollarSign className="h-3.5 w-3.5" />
            <span>${t.prize} premio</span>
          </div>
        </div>
      </div>

      <div className="border-t border-[#2d1747] px-4 py-3">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0 flex-1">
            <div className="mb-1 text-xs text-slate-500">
              {t.status === 'DRAFT' ? 'Pendiente de publicar' : `${t.participants}/${t.totalSlots} slots`}
            </div>
            <div className="h-1.5 w-full overflow-hidden rounded-full bg-white/5">
              <div className="progress-bar h-full rounded-full bg-[#b65cff]" style={{ '--progress': `${t.progress || 8}%` }} />
            </div>
          </div>
          <Link to={actionTo(t)} className="magnetic-action shrink-0 rounded-md border border-slate-500 px-3 py-1.5 text-xs font-black text-white transition-colors hover:border-[#b65cff]">
            {t.cta || 'Ver'}
          </Link>
        </div>
      </div>
    </article>
  )
}
