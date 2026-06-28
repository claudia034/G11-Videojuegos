import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Shield, Users, GitBranch, DollarSign } from 'lucide-react'
import { storage } from '../services/api'
import { tournamentService } from '../services/tournamentService'
import { notificationService } from '../services/notificationService'
import { reportService } from '../services/reportService'
import SectionBlock from '../components/SectionBlock'

export default function AdminProfile() {
  const [metrics, setMetrics] = useState([])
  const [popularTournamentLeaders, setPopularTournamentLeaders] = useState([])
  const currentUser = storage.getUser()
  const displayName = currentUser?.email || 'Administrador'
  const initials = displayName.slice(0, 2).toUpperCase()

  useEffect(() => {
    Promise.all([
      tournamentService.list(),
      notificationService.listUnread().catch(() => []),
      reportService.popularTournamentsByGame().catch(() => [])
    ]).then(([tournaments, notifications, leaders]) => {
      const managed = currentUser?.role === 'ADMIN'
        ? tournaments
        : tournaments.filter((tournament) => tournament.organizerId === currentUser?.id)

      setPopularTournamentLeaders(leaders)
      setMetrics([
        { label: 'Torneos gestionados', value: managed.length, icon: GitBranch, accent: 'text-[#b65cff]' },
        { label: 'Torneos activos', value: managed.filter((tournament) => tournament.status === 'IN_PROGRESS').length, icon: Shield, accent: 'text-[#ff9f1c]' },
        { label: 'Borradores', value: managed.filter((tournament) => tournament.status === 'DRAFT').length, icon: Users, accent: 'text-[#38f8d4]' },
        { label: 'Notificaciones', value: notifications.length, icon: DollarSign, accent: 'text-[#35d978]' }
      ])
    })
  }, [currentUser?.id, currentUser?.role])

  return (
    <div className="space-y-4">
      <SectionBlock eyebrow="Perfil admin" title="Administrador / Organizador" icon={Shield}>
        <div className="grid gap-4">
          <div className="grid gap-4 lg:grid-cols-[0.9fr_1.1fr]">
            <section className="card">
              <div className="flex items-center gap-4">
                <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-[#ff9f1c] to-[#b65cff] text-2xl font-black text-black">{initials}</div>
                <div>
                  <div className="text-2xl font-black">{displayName}</div>
                  <div className="text-sm font-medium text-slate-500">Cuenta autenticada desde backend</div>
                  <div className="mt-2 rounded-full border border-[#ff9f1c]/30 bg-[#ff9f1c]/10 px-3 py-1 text-xs font-black uppercase text-[#ffbf69]">
                    {currentUser?.role}
                  </div>
                </div>
              </div>

              <div className="mt-5 flex flex-wrap gap-2">
                {['Gestion de torneos', 'Revision de disputas', 'Publicacion de brackets', 'Programacion de rondas'].map((permission) => (
                  <span key={permission} className="rounded-md border border-[#2d1747] bg-black/20 px-3 py-1.5 text-xs font-bold text-slate-300">{permission}</span>
                ))}
              </div>

              <div className="mt-5 flex flex-wrap gap-2">
                <Link to="/tournaments/create" className="inline-flex rounded-md bg-[#b65cff] px-4 py-2 text-sm font-black text-white hover:bg-[#a855f7]">
                  Crear torneo
                </Link>
                <Link to="/reports" className="inline-flex rounded-md border border-[#2d1747] px-4 py-2 text-sm font-black text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]">
                  Ver reportes
                </Link>
              </div>
            </section>

            <section className="grid gap-4 sm:grid-cols-2">
              {metrics.map(({ label, value, icon: Icon, accent }) => (
                <div key={label} className="card">
                  <Icon className={`h-5 w-5 ${accent}`} />
                  <div className={`mt-4 text-3xl font-black ${accent}`}>{value}</div>
                  <div className="mt-1 text-xs font-bold uppercase text-slate-500">{label}</div>
                </div>
              ))}
            </section>
          </div>

          <section className="card">
            <div className="eyebrow">Reporte de torneos mas populares</div>
            <div className="mt-2 text-sm font-medium text-slate-400">
              Lectura rapida por juego para que el admin vea donde hay mas traccion y en que torneo conviene enfocar premios, soporte o moderacion.
            </div>
            <div className="mt-4 grid gap-3 md:grid-cols-3">
              {popularTournamentLeaders.length ? popularTournamentLeaders.map((entry) => (
                <article key={entry.gameName} className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                  <div className="text-lg font-black">{entry.gameName}</div>
                  <div className="mt-1 text-sm font-medium text-slate-400">{entry.tournamentName}</div>
                  <div className="mt-2 text-3xl font-black text-[#38f8d4]">{entry.participantCount}</div>
                  <div className="text-xs font-bold uppercase text-slate-500">Inscripciones lider</div>
                  <div className="mt-2 text-[11px] font-bold uppercase text-slate-600">
                    {entry.occupancyRate || 0}% del cupo usado
                  </div>
                </article>
              )) : (
                <div className="text-sm font-medium text-slate-400">No hay reporte disponible todavia.</div>
              )}
            </div>
          </section>
        </div>
      </SectionBlock>
    </div>
  )
}
