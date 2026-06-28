import React, { useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { tournamentService } from '../services/tournamentService'
import { storage } from '../services/api'
import TournamentForm from '../components/TournamentForm'
import { Loader2 } from 'lucide-react'

const toDateTimeInputValue = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const offset = date.getTimezoneOffset()
  return new Date(date.getTime() - offset * 60 * 1000).toISOString().slice(0, 16)
}

export default function EditTournament() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const currentUser = storage.getUser()

  const { data: tournament, isLoading, error } = useQuery({
    queryKey: ['tournament', id],
    queryFn: () => tournamentService.getById(id)
  })

  const updateMutation = useMutation({
    mutationFn: (payload) => tournamentService.update(id, payload),
    onSuccess: (updated) => {
      queryClient.invalidateQueries(['tournament', id])
      queryClient.invalidateQueries(['tournaments'])
      navigate(`/tournaments/${updated.id}`)
    }
  })

  const initialData = useMemo(() => {
    if (!tournament) return null

    return {
      name: tournament.name,
      gameName: tournament.gameName || tournament.game,
      format: tournament.format,
      maxParticipants: tournament.maxParticipants,
      isTeamBased: tournament.isTeamBased,
      minElo: tournament.minElo || 0,
      maxElo: tournament.maxElo || 3000,
      registrationStartAt: toDateTimeInputValue(tournament.registrationStartAt),
      registrationEndAt: toDateTimeInputValue(tournament.registrationEndAt),
      startAt: toDateTimeInputValue(tournament.startAt),
      endAt: toDateTimeInputValue(tournament.endAt),
      prizes: tournament.prizes?.length ? tournament.prizes.map(p => ({
        ...p,
        amount: p.amount?.toString() || '0'
      })) : []
    }
  }, [tournament])

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-[#b65cff]" />
      </div>
    )
  }

  if (error || !tournament) {
    return (
      <div className="card border-red-500/20 bg-red-500/10 text-red-400">
        No se pudo cargar la información del torneo.
      </div>
    )
  }

  if (tournament.status !== 'DRAFT') {
    return (
      <div className="card border-yellow-500/20 bg-yellow-500/10 text-yellow-400">
        Solo puedes editar torneos que se encuentran en estado Borrador (DRAFT).
      </div>
    )
  }

  const canManage = currentUser && (
    currentUser.role === 'ADMIN' ||
    (currentUser.role === 'ORGANIZER' && tournament.organizerId === currentUser.id)
  )

  if (!canManage) {
    return (
      <div className="card border-red-500/20 bg-red-500/10 text-red-400">
        No tienes permisos para editar este torneo.
      </div>
    )
  }

  const handleSubmit = async (form) => {
    const payload = {
      ...form,
      description: tournament.description,
      status: tournament.status,
      maxParticipants: Number(form.maxParticipants),
      minElo: Number(form.minElo),
      maxElo: Number(form.maxElo),
      organizerId: tournament.organizerId,
      rounds: [],
      prizes: form.prizes.map((prize, index) => ({
        position: index + 1,
        name: prize.name,
        description: prize.description || '',
        prizeType: prize.prizeType,
        amount: Number(prize.amount || 0),
        currency: prize.currency || (prize.prizeType === 'POINTS' ? 'PTS' : 'USD')
      }))
    }

    await updateMutation.mutateAsync(payload)
  }

  return (
    <TournamentForm
      initialData={initialData}
      onSubmit={handleSubmit}
      title="Editar Torneo"
      description="Modifica el formato, capacidad, rango ELO, ventanas de registro y premios. Al guardar, regresarás a la vista del torneo."
      submitLabel="Guardar cambios"
    />
  )
}
