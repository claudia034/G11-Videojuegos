import React, { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import { Link, useParams } from 'react-router-dom'
import { CalendarClock, ShieldCheck, Swords, Trophy, Users, Loader2 } from 'lucide-react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import SectionBlock from '../components/SectionBlock'
import BracketBoard from '../components/BracketBoard'
import { tournamentService } from '../services/tournamentService'
import { registrationService } from '../services/registrationService'
import { playerService } from '../services/playerService'
import { matchService } from '../services/matchService'
import { stripeService } from '../services/stripeService'
import { storage } from '../services/api'

const formatDateTime = (value) => {
  if (!value) return 'Sin programar'
  return new Intl.DateTimeFormat('es-SV', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

const toDateTimeLocalValue = (value) => {
  if (!value) return ''
  const date = new Date(value)
  const offset = date.getTimezoneOffset()
  return new Date(date.getTime() - offset * 60 * 1000).toISOString().slice(0, 16)
}

const toScheduleFields = (value) => {
  const localValue = toDateTimeLocalValue(value)
  if (!localValue) return { date: '', time: '' }

  const [date, time] = localValue.split('T')
  return {
    date: date || '',
    time: time || ''
  }
}

export default function TournamentDetail() {
  const { id } = useParams()
  const queryClient = useQueryClient()
  const currentUser = storage.getUser()
  const role = currentUser?.role
  const isAdmin = role === 'ADMIN'
  const isOrganizer = role === 'ORGANIZER'
  const isPlayer = role === 'PLAYER'
  const canManage = isAdmin || isOrganizer

  const { data: tournament, isLoading: loadingT } = useQuery({ queryKey: ['tournament', id], queryFn: () => tournamentService.getById(id) })
  const { data: participants = [], isLoading: loadingP } = useQuery({ queryKey: ['tournamentParticipants', id], queryFn: () => tournamentService.getParticipants(id) })
  const { data: bracket, isLoading: loadingB } = useQuery({ queryKey: ['bracket', id], queryFn: () => tournamentService.getBracket(id), retry: false })
  const { data: matches = [], isLoading: loadingM } = useQuery({ queryKey: ['matches', id], queryFn: () => tournamentService.getMatches(id) })
  const { data: management } = useQuery({ queryKey: ['management', id], queryFn: () => tournamentService.getManagement(id), enabled: canManage, retry: false })
  const { data: currentPlayer } = useQuery({ queryKey: ['currentPlayer'], queryFn: () => playerService.getCurrent(), enabled: isPlayer, retry: false })
  const { data: myRegistrations = [] } = useQuery({ queryKey: ['myRegistrations'], queryFn: () => registrationService.listMine(), enabled: !!currentUser, retry: false })

  const loading = loadingT || loadingP || loadingB || loadingM

  const [scheduleInputs, setScheduleInputs] = useState({})
  const [matchScheduleInputs, setMatchScheduleInputs] = useState({})
  const [resultForms, setResultForms] = useState({})
  const [disputeReasons, setDisputeReasons] = useState({})
  const [resolveForms, setResolveForms] = useState({})
  const [adminDecisionForms, setAdminDecisionForms] = useState({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [showFundingModal, setShowFundingModal] = useState(false)
  const [fundingLoading, setFundingLoading] = useState(false)

  useEffect(() => {
    if (bracket) {
      setScheduleInputs(Object.fromEntries((bracket.rounds || []).map((round) => [round.id, toScheduleFields(round.scheduledStart)])))
    }
  }, [bracket])

  useEffect(() => {
    if (matches.length > 0) {
      setMatchScheduleInputs(Object.fromEntries(matches.map((match) => [match.id, toScheduleFields(match.scheduledAt)])))
    }
  }, [matches])

  const myRegistration = useMemo(
    () => myRegistrations.find((registration) => registration.tournamentId === Number(id)),
    [id, myRegistrations]
  )

  const mutation = useMutation({
    mutationFn: async (action) => await action(),
    onSuccess: () => {
      setMessage('Operacion completada correctamente.')
      queryClient.invalidateQueries({ queryKey: ['tournament', id] })
      queryClient.invalidateQueries({ queryKey: ['tournamentParticipants', id] })
      queryClient.invalidateQueries({ queryKey: ['bracket', id] })
      queryClient.invalidateQueries({ queryKey: ['matches', id] })
      queryClient.invalidateQueries({ queryKey: ['management', id] })
      queryClient.invalidateQueries({ queryKey: ['myRegistrations'] })
    },
    onError: (err) => {
      setError(err.message || 'No se pudo completar la operacion.')
    }
  })

  const submitAction = (action) => {
    setMessage('')
    setError('')
    mutation.mutate(action)
  }

  const handleRegister = () => {
    if (!currentPlayer?.id) {
      setError('No se encontro el perfil de jugador autenticado.')
      return
    }
    submitAction(() => registrationService.registerPlayer(id, currentPlayer.id))
  }

  const handleWithdraw = () => {
    if (!myRegistration?.id) {
      setError('No se encontro una inscripcion activa para retirar.')
      return
    }
    submitAction(() => registrationService.withdraw(myRegistration.id))
  }

  const handleSchedule = (roundId) => {
    const schedule = scheduleInputs[roundId] || { date: '', time: '' }
    if (!schedule.date || !schedule.time) {
      setError('Selecciona la fecha y la hora para la ronda.')
      return
    }
    const scheduledStart = `${schedule.date}T${schedule.time}`
    submitAction(() => tournamentService.scheduleRound(roundId, scheduledStart))
  }

  const handleScheduleMatch = (matchId) => {
    const schedule = matchScheduleInputs[matchId] || { date: '', time: '' }
    if (!schedule.date || !schedule.time) {
      setError('Selecciona la fecha y la hora para el partido.')
      return
    }
    const scheduledAt = `${schedule.date}T${schedule.time}`
    submitAction(() => matchService.schedule(matchId, scheduledAt))
  }

  const handleSubmitResult = (match) => {
    const form = resultForms[match.id] || {}
    if (!form.winnerId) {
      setError('Selecciona el ganador antes de reportar el resultado.')
      return
    }

    submitAction(() => matchService.submitResult(match.id, {
      winnerId: Number(form.winnerId),
      score1: Number(form.score1 || 0),
      score2: Number(form.score2 || 0),
      evidenceUrl: form.evidenceUrl || '',
      notes: form.notes || ''
    }))
  }

  const handleResolveDispute = (match) => {
    const form = resolveForms[match.id] || {}
    if (!form.winnerId) {
      setError('Selecciona un ganador para resolver la disputa.')
      return
    }

    submitAction(() => matchService.resolveDispute(match.id, currentUser.id, {
      winnerId: Number(form.winnerId),
      adminNotes: form.adminNotes || ''
    }))
  }

  const handleAdminDecision = (match) => {
    const form = adminDecisionForms[match.id] || {}
    if (!form.winnerId) {
      setError('Selecciona el ganador para cerrar el partido.')
      return
    }

    submitAction(() => matchService.adminDecision(match.id, {
      winnerId: Number(form.winnerId),
      score1: form.score1 === '' || form.score1 == null ? null : Number(form.score1),
      score2: form.score2 === '' || form.score2 == null ? null : Number(form.score2),
      evidenceUrl: form.evidenceUrl || '',
      adminNotes: form.adminNotes || ''
    }))
  }

  return (
    <div className="space-y-4">
      <SectionBlock
        eyebrow="Torneo"
        title={tournament?.name || 'Detalle del torneo'}
        icon={Trophy}
        action={
          <div className="flex flex-wrap gap-2">
            <Link to={`/spectator/${id}`} className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]">
              Modo espectador
            </Link>
            {bracket?.rounds?.length > 0 && (
              <Link to={`/bracket/${id}`} className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69]">
                Ver bracket
              </Link>
            )}
          </div>
        }
      >
        {message && (
          <div className="rounded-md border border-emerald-400/30 bg-emerald-500/10 px-3 py-2 text-sm font-bold text-emerald-300">
            {message}
          </div>
        )}
        {error && (
          <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">
            {error}
          </div>
        )}

        {loading ? (
          <div className="card text-sm font-medium text-slate-400">Cargando informacion del torneo...</div>
        ) : tournament ? (
          <div className="grid gap-4">
            <div className="grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
              <section className="card">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <div className="eyebrow">{tournament.game}</div>
                    <div className="mt-1 text-2xl font-black">{tournament.name}</div>
                    <div className="mt-2 max-w-2xl text-sm font-medium text-slate-400">
                      {tournament.description || 'Sin descripcion adicional.'}
                    </div>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 px-3 py-2 text-xs font-black uppercase text-slate-300">
                    {tournament.statusLabel}
                  </div>
                </div>

                <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Formato</div>
                    <div className="mt-1 text-lg font-black text-white">{tournament.formatLabel}</div>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Capacidad</div>
                    <div className="mt-1 text-lg font-black text-[#38f8d4]">{participants.length}/{tournament.totalSlots}</div>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Rango ELO</div>
                    <div className="mt-1 text-lg font-black text-[#ff9f1c]">{tournament.minElo} - {tournament.maxElo}</div>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Premio</div>
                    <div className="mt-1 text-lg font-black text-[#b65cff]">${tournament.prize}</div>
                  </div>
                </div>

                <div className="mt-5 grid gap-3 sm:grid-cols-2">
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3 text-sm font-medium text-slate-400">
                    Registro: <span className="font-black text-white">{formatDateTime(tournament.registrationStartAt)}</span> a <span className="font-black text-white">{formatDateTime(tournament.registrationEndAt)}</span>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3 text-sm font-medium text-slate-400">
                    Inicio del torneo: <span className="font-black text-white">{formatDateTime(tournament.startAt)}</span>
                  </div>
                </div>
              </section>

              <section className="card">
                <div className="eyebrow">Acciones</div>
                <div className="mt-4 grid gap-3">
                  {isPlayer && (
                    <>
                      <button
                        type="button"
                        onClick={handleRegister}
                        disabled={Boolean(myRegistration) || tournament.status !== 'REGISTRATION_OPEN'}
                        className="rounded-md bg-[#b65cff] px-4 py-3 text-sm font-black text-white transition-colors hover:bg-[#a855f7] disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {myRegistration ? 'Ya inscrito' : 'Inscribirme'}
                      </button>
                      {myRegistration && (
                        <button
                          type="button"
                          onClick={handleWithdraw}
                          className="rounded-md border border-[#2d1747] px-4 py-3 text-sm font-black text-slate-300 hover:border-red-400/40 hover:text-red-300"
                        >
                          Retirar inscripcion
                        </button>
                      )}
                    </>
                  )}

                  {canManage && (
                    <>
                      <button
                        type="button"
                        onClick={() => submitAction(() => tournamentService.generateRounds(id))}
                        className="rounded-md border border-[#2d1747] px-4 py-3 text-sm font-black text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]"
                      >
                        Regenerar rondas
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          const requiresFunding = tournament.prizes?.some(p => p.prizeType === 'CASH' && p.amount > 0)
                          if (requiresFunding) {
                            setShowFundingModal(true)
                          } else {
                            submitAction(() => tournamentService.publish(id))
                          }
                        }}
                        disabled={tournament.status !== 'DRAFT'}
                        className="rounded-md border border-[#2d1747] px-4 py-3 text-sm font-black text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69] disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        Publicar torneo
                      </button>
                      <button
                        type="button"
                        onClick={() => submitAction(() => tournamentService.closeRegistration(id))}
                        disabled={tournament.status !== 'REGISTRATION_OPEN'}
                        className="rounded-md border border-[#2d1747] px-4 py-3 text-sm font-black text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69] disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        Cerrar inscripciones
                      </button>
                      <button
                        type="button"
                        onClick={() => submitAction(() => tournamentService.generateBracket(id, 'BY_RANKING'))}
                        className="rounded-md border border-[#2d1747] px-4 py-3 text-sm font-black text-slate-300 hover:border-[#b65cff] hover:text-white"
                      >
                        Generar bracket por ranking
                      </button>
                      <button
                        type="button"
                        onClick={() => submitAction(() => tournamentService.generateBracket(id, 'RANDOM'))}
                        className="rounded-md border border-[#2d1747] px-4 py-3 text-sm font-black text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]"
                      >
                        Generar bracket aleatorio
                      </button>
                    </>
                  )}
                </div>
              </section>
            </div>

            {canManage && management && (
              <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <article className="card">
                  <Users className="h-5 w-5 text-[#38f8d4]" />
                  <div className="mt-3 text-3xl font-black text-[#38f8d4]">{management.totalParticipants}</div>
                  <div className="text-xs font-bold uppercase text-slate-500">Participantes totales</div>
                </article>
                <article className="card">
                  <CalendarClock className="h-5 w-5 text-[#ff9f1c]" />
                  <div className="mt-3 text-3xl font-black text-[#ff9f1c]">{management.scheduledMatches}</div>
                  <div className="text-xs font-bold uppercase text-slate-500">Partidos programados</div>
                </article>
                <article className="card">
                  <ShieldCheck className="h-5 w-5 text-[#b65cff]" />
                  <div className="mt-3 text-3xl font-black text-[#b65cff]">{management.completedMatches}</div>
                  <div className="text-xs font-bold uppercase text-slate-500">Resultados cerrados</div>
                </article>
                <article className="card">
                  <Swords className="h-5 w-5 text-red-300" />
                  <div className="mt-3 text-3xl font-black text-red-300">{management.disputedMatches}</div>
                  <div className="text-xs font-bold uppercase text-slate-500">Disputas activas</div>
                </article>
              </section>
            )}

            {isAdmin && management && (
              <section className="card">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <div className="eyebrow">Centro de disputas</div>
                    <div className="mt-1 text-lg font-black">Revision administrativa</div>
                    <div className="mt-2 max-w-3xl text-sm font-medium text-slate-400">
                      Aqui el administrador ve de inmediato cuantas disputas hay, que partido esta bloqueado y en que estado va la revision.
                    </div>
                  </div>
                  <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-black text-red-200">
                    {management.disputedMatches} disputa{management.disputedMatches === 1 ? '' : 's'} activa{management.disputedMatches === 1 ? '' : 's'}
                  </div>
                </div>

                <div className="mt-4 grid gap-3">
                  {management.disputedMatchDetails?.length ? management.disputedMatchDetails.map((match) => (
                    <article key={match.id} className="rounded-xl border border-red-400/20 bg-red-500/5 p-4">
                      <div className="flex flex-wrap items-center justify-between gap-3">
                        <div>
                          <div className="font-black text-white">{match.participant1Name} vs {match.participant2Name}</div>
                          <div className="mt-1 text-xs font-bold uppercase text-red-200">
                            {match.roundName} • {match.status}
                          </div>
                        </div>
                        <a href={`#match-${match.id}`} className="rounded-md border border-red-400/30 px-3 py-2 text-xs font-black uppercase text-red-200 hover:border-[#ff9f1c] hover:text-[#ffbf69]">
                          Ir al partido
                        </a>
                      </div>
                      <div className="mt-3 text-sm font-medium text-slate-300">
                        {match.result?.disputeReason || 'La disputa fue abierta pero aun no hay detalle adicional.'}
                      </div>
                      {match.result?.adminNotes && (
                        <div className="mt-2 text-sm font-medium text-slate-500">
                          Nota admin previa: {match.result.adminNotes}
                        </div>
                      )}
                    </article>
                  )) : (
                    <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4 text-sm font-medium text-slate-400">
                      No hay disputas pendientes. Si un jugador impugna un resultado, aparecera aqui para revision.
                    </div>
                  )}
                </div>
              </section>
            )}

            <section className="card">
              <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <div className="eyebrow">Participantes</div>
                  <div className="mt-1 text-lg font-black">Inscritos y seeds</div>
                </div>
                <div className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300">
                  Limite {tournament.totalSlots}
                </div>
              </div>

              <div className="grid gap-3 md:grid-cols-2">
                {participants.length ? participants.map((participant) => (
                  <article key={participant.id} className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <div className="font-black">{participant.participantName}</div>
                        <div className="text-sm font-medium text-slate-400">
                          {participant.participantType} • ELO {participant.eloAtRegistration ?? 'N/A'}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-black text-[#38f8d4]">#{participant.seed ?? '-'}</div>
                        <div className="text-xs font-bold uppercase text-slate-500">{participant.status}</div>
                      </div>
                    </div>
                  </article>
                )) : (
                  <div className="text-sm font-medium text-slate-400">
                    Aun no hay participantes registrados.
                  </div>
                )}
              </div>
            </section>

            <section className="card">
              <div className="eyebrow">Premios y asignaciones</div>
              <div className="mt-3 grid gap-3 lg:grid-cols-[0.7fr_1.3fr]">
                <div className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                  <div className="text-sm font-bold uppercase text-slate-500">Como funciona</div>
                  <div className="mt-3 grid gap-3">
                    <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                      <div className="text-sm font-black text-[#38f8d4]">Puntos virtuales</div>
                      <div className="mt-1 text-sm font-medium text-slate-400">
                        Si el premio es de tipo <span className="font-black text-slate-200">POINTS</span>, al cerrar el torneo esos puntos se suman automaticamente al ranking global del jugador ganador.
                      </div>
                    </div>
                    <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                      <div className="text-sm font-black text-[#ffbf69]">Premios fisicos o monetarios</div>
                      <div className="mt-1 text-sm font-medium text-slate-400">
                        Los premios <span className="font-black text-slate-200">CASH</span>, <span className="font-black text-slate-200">ITEM</span> y <span className="font-black text-slate-200">OTHER</span> quedan asignados al jugador segun la posicion final del torneo.
                      </div>
                    </div>
                    <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                      <div className="text-sm font-black text-[#b65cff]">Validacion del admin</div>
                      <div className="mt-1 text-sm font-medium text-slate-400">
                        Si nadie reporta la final, el admin puede cerrar el partido manualmente y asi liberar la asignacion de premios y puntos.
                      </div>
                    </div>
                  </div>
                </div>

                <div className="grid gap-3 md:grid-cols-2">
                {(tournament.prizes || []).length ? tournament.prizes.map((prize) => (
                  <article key={prize.id || prize.position} className="rounded-md border border-[#2d1747] bg-black/20 p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="text-lg font-black text-white">#{prize.position} {prize.name}</div>
                        <div className="text-sm font-medium text-slate-400">{prize.description || 'Premio configurado para el torneo.'}</div>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-black text-[#ff9f1c]">
                          {prize.amount ? `${prize.amount} ${prize.currency || ''}`.trim() : prize.prizeType}
                        </div>
                        <div className="text-xs font-bold uppercase text-slate-500">{prize.prizeType}</div>
                      </div>
                    </div>
                    <div className="mt-3 text-sm font-bold text-slate-300">
                      {prize.playerUsername ? `Asignado a ${prize.playerUsername}` : 'Aun sin asignar'}
                    </div>
                    <div className="mt-2 text-xs font-bold uppercase text-slate-500">
                      {prize.prizeType === 'POINTS' ? 'Creditos virtuales para ranking' : 'Entrega administrada por organizacion'}
                    </div>
                  </article>
                )) : (
                  <div className="text-sm font-medium text-slate-400">
                    Este torneo todavia no tiene premios configurados.
                  </div>
                )}
                </div>
              </div>
            </section>

            <section className="card">
              <BracketBoard tournamentName={tournament.name} rounds={bracket?.rounds || []} />
            </section>

            {canManage && bracket?.rounds?.length > 0 && (
              <section className="card">
                <div className="eyebrow">Programacion de rondas</div>
                <div className="mt-4 grid gap-3">
                  {bracket.rounds.map((round) => (
                    <article key={round.id} className="rounded-md border border-[#2d1747] bg-black/20 p-4">
                      <div className="flex flex-wrap items-center justify-between gap-3">
                        <div>
                          <div className="font-black">{round.name}</div>
                          <div className="text-sm font-medium text-slate-400">
                            Estado {round.status} • Actual {formatDateTime(round.scheduledStart)}
                          </div>
                        </div>
                        <div className="flex flex-wrap gap-2">
                          <input
                            type="date"
                            value={scheduleInputs[round.id]?.date || ''}
                            onChange={(event) => setScheduleInputs((current) => ({
                              ...current,
                              [round.id]: { ...(current[round.id] || { date: '', time: '' }), date: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 px-3 py-2 text-sm text-white outline-none focus:border-[#b65cff]"
                          />
                          <input
                            type="time"
                            value={scheduleInputs[round.id]?.time || ''}
                            onChange={(event) => setScheduleInputs((current) => ({
                              ...current,
                              [round.id]: { ...(current[round.id] || { date: '', time: '' }), time: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 px-3 py-2 text-sm text-white outline-none focus:border-[#b65cff]"
                          />
                          <button
                            type="button"
                            onClick={() => handleSchedule(round.id)}
                            className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]"
                          >
                            Guardar
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              </section>
            )}

            <section className="card">
              <div className="eyebrow">Partidos y resultados</div>
              <div className="mt-4 grid gap-4">
                {matches.length ? matches.map((match) => {
                  const form = resultForms[match.id] || {}
                  const resolveForm = resolveForms[match.id] || {}
                  const isFinalMatch = !match.nextMatchId
                  const canStartMatch = (isPlayer || isOrganizer) && match.status === 'SCHEDULED'
                  const canConfirmMatch = (isPlayer || isOrganizer) && match.status === 'RESULT_SUBMITTED'
                  const canReportResult = (isPlayer || isOrganizer) && match.status === 'IN_PROGRESS'
                  const canOpenDispute = (isPlayer || isOrganizer) && (
                    match.status === 'RESULT_SUBMITTED' || (match.status === 'COMPLETED' && isFinalMatch)
                  )
                  return (
                    <article id={`match-${match.id}`} key={match.id} className="rounded-xl border border-[#2d1747] bg-black/20 p-4">
                      <div className="flex flex-wrap items-start justify-between gap-4">
                        <div>
                          <div className="font-black text-white">{match.participant1Name} vs {match.participant2Name}</div>
                          <div className="mt-1 text-xs font-bold uppercase text-slate-500">
                            {match.roundName} • {match.status} • {formatDateTime(match.scheduledAt)}
                          </div>
                          {match.result && (
                            <div className="mt-2 text-sm font-medium text-slate-300">
                              Score reportado: {match.result.score1 ?? 0} - {match.result.score2 ?? 0}
                              {match.result.disputeReason ? ` • Disputa: ${match.result.disputeReason}` : ''}
                            </div>
                          )}
                        </div>

                        <div className="flex flex-wrap gap-2">
                          {canManage && (
                            <>
                              <input
                                type="date"
                                value={matchScheduleInputs[match.id]?.date || ''}
                                onChange={(event) => setMatchScheduleInputs((current) => ({
                                  ...current,
                                  [match.id]: { ...(current[match.id] || { date: '', time: '' }), date: event.target.value }
                                }))}
                                className="rounded-md border border-[#2d1747] bg-black/25 px-3 py-2 text-xs text-white outline-none focus:border-[#b65cff]"
                              />
                              <input
                                type="time"
                                value={matchScheduleInputs[match.id]?.time || ''}
                                onChange={(event) => setMatchScheduleInputs((current) => ({
                                  ...current,
                                  [match.id]: { ...(current[match.id] || { date: '', time: '' }), time: event.target.value }
                                }))}
                                className="rounded-md border border-[#2d1747] bg-black/25 px-3 py-2 text-xs text-white outline-none focus:border-[#b65cff]"
                              />
                              <button
                                type="button"
                                onClick={() => handleScheduleMatch(match.id)}
                                className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#b65cff] hover:text-white"
                              >
                                Programar
                              </button>
                            </>
                          )}
                          {(canStartMatch || canConfirmMatch) && (
                            <>
                              {canStartMatch && (
                              <button
                                type="button"
                                onClick={() => submitAction(() => matchService.start(match.id))}
                                className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69]"
                              >
                                Iniciar
                              </button>
                              )}
                              {canConfirmMatch && (
                              <button
                                type="button"
                                onClick={() => submitAction(() => matchService.confirmResult(match.id))}
                                className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]"
                              >
                                Confirmar
                              </button>
                              )}
                            </>
                          )}
                        </div>
                      </div>

                      {canReportResult && (
                        <div className="mt-4 grid gap-3 lg:grid-cols-[1.2fr_0.8fr]">
                          <div className="grid gap-3">
                            <div className="rounded-md border border-[#b65cff]/20 bg-[#b65cff]/5 p-3 text-sm font-medium text-slate-300">
                              Reporta el resultado solo cuando la partida ya termino. Aqui defines ganador, marcador y evidencia opcional.
                            </div>
                            <select
                              value={form.winnerId || ''}
                              onChange={(event) => setResultForms((current) => ({
                                ...current,
                                [match.id]: { ...current[match.id], winnerId: event.target.value }
                              }))}
                              className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#b65cff]"
                            >
                              <option value="">Selecciona ganador</option>
                              <option value={match.participant1RegistrationId}>{match.participant1Name}</option>
                              <option value={match.participant2RegistrationId}>{match.participant2Name}</option>
                            </select>
                            <div className="grid gap-3 sm:grid-cols-2">
                              <input
                                type="number"
                                min="0"
                                placeholder={`${match.participant1Name} score`}
                                value={form.score1 || ''}
                                onChange={(event) => setResultForms((current) => ({
                                  ...current,
                                  [match.id]: { ...current[match.id], score1: event.target.value }
                                }))}
                                className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#b65cff]"
                              />
                              <input
                                type="number"
                                min="0"
                                placeholder={`${match.participant2Name} score`}
                                value={form.score2 || ''}
                                onChange={(event) => setResultForms((current) => ({
                                  ...current,
                                  [match.id]: { ...current[match.id], score2: event.target.value }
                                }))}
                                className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#b65cff]"
                              />
                            </div>
                            <input
                              type="url"
                              placeholder="URL de captura o evidencia (opcional)"
                              value={form.evidenceUrl || ''}
                              onChange={(event) => setResultForms((current) => ({
                                ...current,
                                [match.id]: { ...current[match.id], evidenceUrl: event.target.value }
                              }))}
                              className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#b65cff]"
                            />
                            <textarea
                              rows="2"
                              placeholder="Notas del resultado"
                              value={form.notes || ''}
                              onChange={(event) => setResultForms((current) => ({
                                ...current,
                                [match.id]: { ...current[match.id], notes: event.target.value }
                              }))}
                              className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#b65cff]"
                            />
                            <button
                              type="button"
                              onClick={() => handleSubmitResult(match)}
                              className="w-fit rounded-md bg-[#b65cff] px-4 py-2 text-sm font-black text-white hover:bg-[#a855f7]"
                            >
                              Reportar resultado
                            </button>
                          </div>
                        </div>
                      )}

                      {canOpenDispute && (
                        <div className="mt-4 grid gap-3 rounded-md border border-[#ff9f1c]/20 bg-[#ff9f1c]/5 p-4">
                          <div className="text-sm font-bold uppercase text-[#ffbf69]">Impugnar resultado reportado</div>
                          <div className="text-sm font-medium text-slate-300">
                            {match.status === 'COMPLETED' && isFinalMatch
                              ? 'La final ya fue cerrada, pero aun puedes pedir revision administrativa. Esto reabre la validacion de premios y puntos para que el admin revise el caso.'
                              : 'Usa esto solo si el rival envio un marcador incorrecto o eligio mal al ganador. El administrador revisara tu comentario antes de validar el partido.'}
                          </div>
                          <textarea
                            rows="4"
                            placeholder={match.status === 'COMPLETED' && isFinalMatch
                              ? 'Explica por que la final debe revisarse nuevamente'
                              : 'Explica que esta mal en el resultado reportado'}
                            value={disputeReasons[match.id] || ''}
                            onChange={(event) => setDisputeReasons((current) => ({ ...current, [match.id]: event.target.value }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                          />
                          <button
                            type="button"
                            onClick={() => submitAction(() => matchService.disputeResult(match.id, disputeReasons[match.id] || 'Revision solicitada'))}
                            className="w-fit rounded-md border border-[#ff9f1c]/40 px-4 py-2 text-xs font-black uppercase text-[#ffbf69] hover:border-[#ff9f1c] hover:text-white"
                          >
                            {match.status === 'COMPLETED' && isFinalMatch ? 'Solicitar revision de la final' : 'Enviar disputa al admin'}
                          </button>
                        </div>
                      )}

                      {!canReportResult && !canOpenDispute && match.status === 'COMPLETED' && (
                        <div className="mt-4 rounded-md border border-emerald-400/20 bg-emerald-500/5 p-3 text-sm font-medium text-slate-300">
                          Este resultado ya fue validado y el partido quedo cerrado.
                        </div>
                      )}

                      {!canReportResult && !canOpenDispute && match.status === 'SCHEDULED' && (
                        <div className="mt-4 rounded-md border border-[#2d1747] bg-black/20 p-3 text-sm font-medium text-slate-400">
                          Primero se debe iniciar la partida. Cuando termine, aqui aparecera el formulario para reportar el resultado.
                        </div>
                      )}

                      {!canReportResult && !canOpenDispute && match.status === 'DISPUTED' && (
                        <div className="mt-4 rounded-md border border-red-400/20 bg-red-500/5 p-3 text-sm font-medium text-red-200">
                          La disputa ya fue enviada y esta esperando la decision del administrador.
                        </div>
                      )}

                      {isAdmin && match.status === 'DISPUTED' && (
                        <div className="mt-4 grid gap-3 rounded-md border border-red-400/20 bg-red-500/5 p-4">
                          <div className="text-sm font-bold uppercase text-red-300">Resolver disputa</div>
                          <select
                            value={resolveForm.winnerId || ''}
                            onChange={(event) => setResolveForms((current) => ({
                              ...current,
                              [match.id]: { ...current[match.id], winnerId: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                          >
                            <option value="">Selecciona ganador definitivo</option>
                            <option value={match.participant1RegistrationId}>{match.participant1Name}</option>
                            <option value={match.participant2RegistrationId}>{match.participant2Name}</option>
                          </select>
                          <textarea
                            rows="3"
                            placeholder="Notas del administrador"
                            value={resolveForm.adminNotes || ''}
                            onChange={(event) => setResolveForms((current) => ({
                              ...current,
                              [match.id]: { ...current[match.id], adminNotes: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                          />
                          <button
                            type="button"
                            onClick={() => handleResolveDispute(match)}
                            className="w-fit rounded-md bg-[#ff9f1c] px-4 py-2 text-sm font-black text-black hover:bg-[#ffbf69]"
                          >
                            Resolver disputa
                          </button>
                        </div>
                      )}

                      {isAdmin && match.status !== 'COMPLETED' && match.status !== 'BYE' && (
                        <div className="mt-4 grid gap-3 rounded-md border border-[#ff9f1c]/30 bg-[#ff9f1c]/5 p-4">
                          <div className="text-sm font-bold uppercase text-[#ffbf69]">Decision administrativa</div>
                          <div className="text-sm font-medium text-slate-400">
                            Usa este bloque si quieres cerrar la partida manualmente, elegir ganador y validar puntos sin depender del reporte de los jugadores.
                          </div>
                          <select
                            value={adminDecisionForms[match.id]?.winnerId || ''}
                            onChange={(event) => setAdminDecisionForms((current) => ({
                              ...current,
                              [match.id]: { ...current[match.id], winnerId: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                          >
                            <option value="">Selecciona ganador definitivo</option>
                            <option value={match.participant1RegistrationId}>{match.participant1Name}</option>
                            <option value={match.participant2RegistrationId}>{match.participant2Name}</option>
                          </select>
                          <div className="grid gap-3 sm:grid-cols-2">
                            <input
                              type="number"
                              min="0"
                              placeholder={`${match.participant1Name} score`}
                              value={adminDecisionForms[match.id]?.score1 || ''}
                              onChange={(event) => setAdminDecisionForms((current) => ({
                                ...current,
                                [match.id]: { ...current[match.id], score1: event.target.value }
                              }))}
                              className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                            />
                            <input
                              type="number"
                              min="0"
                              placeholder={`${match.participant2Name} score`}
                              value={adminDecisionForms[match.id]?.score2 || ''}
                              onChange={(event) => setAdminDecisionForms((current) => ({
                                ...current,
                                [match.id]: { ...current[match.id], score2: event.target.value }
                              }))}
                              className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                            />
                          </div>
                          <input
                            type="url"
                            placeholder="URL de evidencia opcional"
                            value={adminDecisionForms[match.id]?.evidenceUrl || ''}
                            onChange={(event) => setAdminDecisionForms((current) => ({
                              ...current,
                              [match.id]: { ...current[match.id], evidenceUrl: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                          />
                          <textarea
                            rows="3"
                            placeholder="Notas del admin sobre la validacion"
                            value={adminDecisionForms[match.id]?.adminNotes || ''}
                            onChange={(event) => setAdminDecisionForms((current) => ({
                              ...current,
                              [match.id]: { ...current[match.id], adminNotes: event.target.value }
                            }))}
                            className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-sm text-white outline-none focus:border-[#ff9f1c]"
                          />
                          <button
                            type="button"
                            onClick={() => handleAdminDecision(match)}
                            className="w-fit rounded-md bg-[#ff9f1c] px-4 py-2 text-sm font-black text-black hover:bg-[#ffbf69]"
                          >
                            Cerrar partido y validar puntos
                          </button>
                        </div>
                      )}
                    </article>
                  )
                }) : (
                  <div className="text-sm font-medium text-slate-400">
                    Aun no hay partidas generadas para este torneo.
                  </div>
                )}
              </div>
            </section>
          </div>
        ) : (
          <div className="card text-sm font-medium text-slate-400">No se encontro informacion del torneo.</div>
        )}
      </SectionBlock>

      {/* MODAL DE PAGO (STRIPE) */}
      {showFundingModal && createPortal(
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-xl border border-[#2d1747] bg-[#0b0413] p-6 shadow-2xl">
            <h3 className="text-xl font-black text-white">Fondeo de Premios Requerido</h3>
            <p className="mt-2 text-sm font-medium text-slate-400">
              Este torneo tiene configurados premios en efectivo (CASH). Debes fondear los premios a través de nuestra plataforma segura de pagos antes de poder publicar el torneo y abrir las inscripciones.
            </p>
            <div className="mt-6 flex flex-wrap items-center justify-end gap-3">
              <button
                type="button"
                disabled={fundingLoading}
                onClick={() => setShowFundingModal(false)}
                className="rounded-md px-4 py-2 text-sm font-bold text-slate-400 hover:text-white disabled:opacity-50"
              >
                Cancelar
              </button>
              <button
                type="button"
                disabled={fundingLoading}
                onClick={async () => {
                  setFundingLoading(true)
                  try {
                    const response = await stripeService.checkoutTournament(id)
                    window.location.href = response.url
                  } catch (err) {
                    setError(err.message || 'No se pudo iniciar el pago.')
                    setFundingLoading(false)
                    setShowFundingModal(false)
                  }
                }}
                className="inline-flex items-center gap-2 rounded-md bg-[#b65cff] px-4 py-2 text-sm font-black text-white hover:bg-[#a855f7] disabled:opacity-50"
              >
                {fundingLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                Clic aquí para realizar el pago
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  )
}
