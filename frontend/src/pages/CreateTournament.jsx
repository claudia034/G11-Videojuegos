import React from 'react'
import { useNavigate } from 'react-router-dom'
import { tournamentService } from '../services/tournamentService'
import { storage } from '../services/api'
import TournamentForm from '../components/TournamentForm'

const toDateTimeInputValue = (date) => {
  const offset = date.getTimezoneOffset()
  return new Date(date.getTime() - offset * 60 * 1000).toISOString().slice(0, 16)
}

const createInitialForm = () => {
  const now = new Date()
  const registrationEndAt = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000)
  const startAt = new Date(now.getTime() + 5 * 24 * 60 * 60 * 1000)
  const endAt = new Date(startAt.getTime() + 24 * 60 * 60 * 1000)

  return {
    name: '',
    gameName: '',
    format: 'SINGLE_ELIMINATION',
    maxParticipants: 16,
    isTeamBased: false,
    minElo: 0,
    maxElo: 3000,
    registrationStartAt: toDateTimeInputValue(now),
    registrationEndAt: toDateTimeInputValue(registrationEndAt),
    startAt: toDateTimeInputValue(startAt),
    endAt: toDateTimeInputValue(endAt),
    prizes: [
      {
        position: 1,
        name: 'Premio principal',
        description: 'Premio monetario para el campeon',
        prizeType: 'CASH',
        amount: '25',
        currency: 'USD'
      },
      {
        position: 2,
        name: 'Premio #2',
        description: 'Puntos virtuales para el subcampeon',
        prizeType: 'POINTS',
        amount: '50',
        currency: 'PTS'
      }
    ]
  }
}

export default function CreateTournament() {
  const navigate = useNavigate()

  const handleSubmit = async (form) => {
    const currentUser = storage.getUser()
    if (!currentUser) {
      throw new Error('Inicia sesion antes de crear un torneo.')
    }

    const payload = {
      ...form,
      description: `Creado desde la UI para ${form.gameName} en formato ${form.format}.`,
      status: 'DRAFT',
      maxParticipants: Number(form.maxParticipants),
      minElo: Number(form.minElo),
      maxElo: Number(form.maxElo),
      organizerId: currentUser.id,
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

    const created = await tournamentService.create(payload)
    navigate(`/tournaments/${created.id}`)
  }

  return (
    <TournamentForm
      initialData={createInitialForm()}
      onSubmit={handleSubmit}
      title="Crear Torneo"
      description="Configura el formato, capacidad, rango ELO y ventanas de registro sin romper la arquitectura del backend."
      submitLabel="Crear torneo"
    />
  )
}
