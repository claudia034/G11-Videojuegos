import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { tournamentService } from '../services/tournamentService'
import { tournamentFormatService } from '../services/tournamentFormatService'
import { storage } from '../services/api'

const defaultFormat = 'SINGLE_ELIMINATION'
const allowedFormats = new Set([
  'SINGLE_ELIMINATION',
  'DOUBLE_ELIMINATION',
  'ROUND_ROBIN',
  'SWISS'
])

const prizeTypeOptions = [
  { value: 'CASH', label: 'Dinero', hint: 'Premio monetario con monto y moneda.' },
  { value: 'POINTS', label: 'Puntos', hint: 'Creditos virtuales para el ranking global.' },
  { value: 'ITEM', label: 'Item', hint: 'Perifericos, giftcards, skins o productos fisicos.' },
  { value: 'OTHER', label: 'Otro', hint: 'Reconocimientos, acceso VIP o premios personalizados.' }
]

const createPrize = (position, overrides = {}) => ({
  position,
  name: position === 1 ? 'Premio principal' : `Premio #${position}`,
  description: '',
  prizeType: position === 1 ? 'CASH' : 'POINTS',
  amount: position === 1 ? '25' : '100',
  currency: position === 1 ? 'USD' : 'PTS',
  ...overrides
})

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
    format: defaultFormat,
    maxParticipants: 16,
    isTeamBased: false,
    minElo: 0,
    maxElo: 3000,
    registrationStartAt: toDateTimeInputValue(now),
    registrationEndAt: toDateTimeInputValue(registrationEndAt),
    startAt: toDateTimeInputValue(startAt),
    endAt: toDateTimeInputValue(endAt),
    prizes: [
      createPrize(1, {
        description: 'Premio monetario para el campeon'
      }),
      createPrize(2, {
        prizeType: 'POINTS',
        amount: '50',
        currency: 'PTS',
        description: 'Puntos virtuales para el subcampeon'
      })
    ]
  }
}

export default function CreateTournament() {
  const navigate = useNavigate()
  const [form, setForm] = useState(createInitialForm())
  const [formats, setFormats] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loadingFormats, setLoadingFormats] = useState(true)

  useEffect(() => {
    tournamentFormatService.list()
      .then((items) => {
        const filteredItems = items.filter((item) => allowedFormats.has(item.format))
        setFormats(filteredItems)
        if (filteredItems.length > 0) {
          setForm((current) => ({
            ...current,
            format: filteredItems.some((item) => item.format === current.format) ? current.format : filteredItems[0].format
          }))
        }
      })
      .catch(() => {
        setFormats([])
      })
      .finally(() => setLoadingFormats(false))
  }, [])

  const handle = (event) => {
    const { name, value, type, checked } = event.target
    setForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const handlePrizeChange = (index, field, value) => {
    setForm((current) => ({
      ...current,
      prizes: current.prizes.map((prize, prizeIndex) => {
        if (prizeIndex !== index) return prize

        const nextPrize = {
          ...prize,
          [field]: value
        }

        if (field === 'prizeType') {
          if (value === 'POINTS') {
            nextPrize.currency = 'PTS'
            if (!nextPrize.amount) nextPrize.amount = '100'
          } else if (nextPrize.currency === 'PTS') {
            nextPrize.currency = 'USD'
          }
        }

        return nextPrize
      })
    }))
  }

  const addPrize = () => {
    setForm((current) => ({
      ...current,
      prizes: [...current.prizes, createPrize(current.prizes.length + 1)]
    }))
  }

  const removePrize = (index) => {
    setForm((current) => ({
      ...current,
      prizes: current.prizes
        .filter((_, prizeIndex) => prizeIndex !== index)
        .map((prize, prizeIndex) => ({
          ...prize,
          position: prizeIndex + 1,
          name: prize.name || `Premio #${prizeIndex + 1}`
        }))
    }))
  }

  const submit = async (event) => {
    event.preventDefault()
    setMessage('')
    setError('')

    try {
      const currentUser = storage.getUser()
      if (!currentUser) {
        throw new Error('Inicia sesion antes de crear un torneo.')
      }

      const selectedFormat = formats.find((item) => item.format === form.format)
      const registrationStartAt = new Date(form.registrationStartAt)
      const registrationEndAt = new Date(form.registrationEndAt)
      const startAt = new Date(form.startAt)
      const endAt = new Date(form.endAt)

      if (selectedFormat?.maximumParticipants && Number(form.maxParticipants) > selectedFormat.maximumParticipants) {
        throw new Error(`El formato ${selectedFormat.displayName} soporta hasta ${selectedFormat.maximumParticipants} participantes.`)
      }

      if (Number(form.minElo) > Number(form.maxElo)) {
        throw new Error('El ELO minimo no puede ser mayor al ELO maximo.')
      }

      if (!form.registrationStartAt || !form.registrationEndAt || !form.startAt || !form.endAt) {
        throw new Error('Debes seleccionar fecha y hora para registro e inicio del torneo.')
      }

      if (registrationStartAt > registrationEndAt) {
        throw new Error('El inicio de registro no puede ser posterior al cierre de registro.')
      }

      if (registrationEndAt > startAt) {
        throw new Error('El cierre de registro no puede ser posterior al inicio del torneo.')
      }

      if (startAt > endAt) {
        throw new Error('La fecha de inicio del torneo no puede ser posterior a la fecha de cierre.')
      }

      const created = await tournamentService.create({
        name: form.name,
        description: `Creado desde la UI para ${form.gameName} en formato ${form.format}.`,
        gameName: form.gameName,
        format: form.format,
        status: 'DRAFT',
        maxParticipants: Number(form.maxParticipants),
        isTeamBased: form.isTeamBased,
        minElo: Number(form.minElo),
        maxElo: Number(form.maxElo),
        organizerId: currentUser.id,
        registrationStartAt: form.registrationStartAt,
        registrationEndAt: form.registrationEndAt,
        startAt: form.startAt,
        endAt: form.endAt,
        rounds: [],
        prizes: form.prizes.map((prize, index) => ({
          position: index + 1,
          name: prize.name,
          description: prize.description || '',
          prizeType: prize.prizeType,
          amount: Number(prize.amount || 0),
          currency: prize.currency || (prize.prizeType === 'POINTS' ? 'PTS' : 'USD')
        }))
      })

      navigate(`/tournaments/${created.id}`)
    } catch (err) {
      setError(err.message || 'No se pudo crear el torneo.')
    }
  }

  const selectedFormat = formats.find((item) => item.format === form.format)
  const formatsByFamily = formats.reduce((groups, item) => {
    const family = item.family || 'OTHER'
    if (!groups[family]) groups[family] = []
    groups[family].push(item)
    return groups
  }, {})
  const familyLabelMap = {
    SINGLE_ELIMINATION: 'Eliminacion simple',
    DOUBLE_ELIMINATION: 'Doble eliminacion',
    ROUND_ROBIN: 'Round robin',
    SWISS: 'Swiss'
  }

  return (
    <div className="space-y-4">
      <div>
        <div className="eyebrow">Organizacion</div>
        <h1 className="mt-1 text-2xl font-black">Crear Torneo</h1>
        <p className="mt-2 max-w-2xl text-sm text-slate-400">
          Configura el formato, capacidad, rango ELO y ventanas de registro sin romper la arquitectura del backend.
        </p>
        <div className="mt-3 inline-flex rounded-md border border-[#2d1747] bg-black/20 px-3 py-2 text-xs font-black uppercase text-[#38f8d4]">
          Catalogo activo: {formats.length || 0} formatos base
        </div>
      </div>

      <form onSubmit={submit} className="card grid max-w-4xl gap-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Nombre
            <input name="name" value={form.name} onChange={handle} placeholder="Night Cup #13" className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Juego
            <input name="gameName" value={form.gameName} onChange={handle} placeholder="Valorant" className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Formato
            <select
              name="format"
              value={form.format}
              onChange={handle}
              disabled={loadingFormats}
              className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
            >
              {formats.length ? Object.entries(formatsByFamily).map(([family, items]) => (
                <optgroup key={family} label={familyLabelMap[family] || family}>
                  {items.map((format) => (
                    <option key={format.format} value={format.format}>
                      {format.displayName}
                    </option>
                  ))}
                </optgroup>
              )) : (
                <option value={defaultFormat}>Eliminacion simple</option>
              )}
            </select>
          </label>

          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Cupo maximo
            <input name="maxParticipants" value={form.maxParticipants} type="number" min="2" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>

          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Modalidad
            <span className="flex h-full items-center rounded-md border border-[#2d1747] bg-black/25 px-3 text-white">
              <input name="isTeamBased" type="checkbox" checked={form.isTeamBased} onChange={handle} className="mr-3 h-4 w-4 accent-[#38f8d4]" />
              {form.isTeamBased ? 'Por equipos' : 'Individual'}
            </span>
          </label>
        </div>

        <section className="grid gap-4 rounded-xl border border-[#2d1747] bg-black/20 p-4">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <div className="eyebrow">Premios</div>
              <div className="mt-1 text-lg font-black text-white">Configura dinero, puntos, items o premios personalizados</div>
              <div className="mt-2 max-w-2xl text-sm font-medium text-slate-400">
                Ya no estas limitado a un solo premio en efectivo. Puedes mezclar creditos virtuales, productos fisicos, giftcards o cualquier reconocimiento que quieras entregar.
              </div>
            </div>
            <button
              type="button"
              onClick={addPrize}
              className="rounded-md border border-[#2d1747] px-4 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]"
            >
              Agregar premio
            </button>
          </div>

          <div className="grid gap-4">
            {form.prizes.map((prize, index) => {
              const selectedPrizeType = prizeTypeOptions.find((option) => option.value === prize.prizeType)
              return (
                <article key={`${prize.position}-${index}`} className="rounded-xl border border-[#2d1747] bg-black/25 p-4">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <div className="text-lg font-black text-white">Posicion #{prize.position}</div>
                      <div className="text-sm font-medium text-slate-500">{selectedPrizeType?.hint}</div>
                    </div>
                    {form.prizes.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removePrize(index)}
                        className="rounded-md border border-red-400/20 px-3 py-2 text-xs font-black uppercase text-red-200 hover:border-red-400 hover:text-white"
                      >
                        Quitar
                      </button>
                    )}
                  </div>

                  <div className="mt-4 grid gap-4 md:grid-cols-2">
                    <label className="grid gap-2 text-sm font-bold text-slate-300">
                      Nombre del premio
                      <input
                        value={prize.name}
                        onChange={(event) => handlePrizeChange(index, 'name', event.target.value)}
                        placeholder="Gift card Steam"
                        className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                      />
                    </label>
                    <label className="grid gap-2 text-sm font-bold text-slate-300">
                      Tipo
                      <select
                        value={prize.prizeType}
                        onChange={(event) => handlePrizeChange(index, 'prizeType', event.target.value)}
                        className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                      >
                        {prizeTypeOptions.map((option) => (
                          <option key={option.value} value={option.value}>{option.label}</option>
                        ))}
                      </select>
                    </label>
                  </div>

                  <div className="mt-4 grid gap-4 sm:grid-cols-2">
                    <label className="grid gap-2 text-sm font-bold text-slate-300">
                      Descripcion
                      <input
                        value={prize.description}
                        onChange={(event) => handlePrizeChange(index, 'description', event.target.value)}
                        placeholder="Skin exclusiva, mouse gamer o creditos de ranking"
                        className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                      />
                    </label>
                    <label className="grid gap-2 text-sm font-bold text-slate-300">
                      Monto
                      <input
                        value={prize.amount}
                        type="number"
                        min="0"
                        onChange={(event) => handlePrizeChange(index, 'amount', event.target.value)}
                        className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                      />
                    </label>
                  </div>

                  <div className="mt-4 grid gap-4 sm:grid-cols-2">
                    <label className="grid gap-2 text-sm font-bold text-slate-300">
                      Moneda / Codigo
                      <input
                        value={prize.currency}
                        maxLength="3"
                        onChange={(event) => handlePrizeChange(index, 'currency', event.target.value.toUpperCase())}
                        placeholder={prize.prizeType === 'POINTS' ? 'PTS' : 'USD'}
                        className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                      />
                    </label>
                    <div className="rounded-md border border-[#2d1747] bg-black/20 p-3 text-sm font-medium text-slate-400">
                      {prize.prizeType === 'POINTS'
                        ? 'Este premio se convertira en puntos virtuales para el ranking global del jugador.'
                        : prize.prizeType === 'ITEM'
                          ? 'Usa la descripcion para especificar el producto o regalo fisico que recibira el ganador.'
                          : prize.prizeType === 'OTHER'
                            ? 'Ideal para beneficios especiales, acceso a eventos, coaching o reconocimientos personalizados.'
                            : 'Usa monto y moneda para indicar el valor monetario del premio.'}
                    </div>
                  </div>
                </article>
              )
            })}
          </div>
        </section>

        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            ELO minimo
            <input name="minElo" value={form.minElo} type="number" min="0" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            ELO maximo
            <input name="maxElo" value={form.maxElo} type="number" min="0" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Inicio de registro
            <input name="registrationStartAt" value={form.registrationStartAt} type="datetime-local" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Cierre de registro
            <input name="registrationEndAt" value={form.registrationEndAt} type="datetime-local" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Inicio del torneo
            <input name="startAt" value={form.startAt} type="datetime-local" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
          <label className="grid gap-2 text-sm font-bold text-slate-300">
            Cierre estimado
            <input name="endAt" value={form.endAt} type="datetime-local" onChange={handle} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" />
          </label>
        </div>

        {selectedFormat && (
          <div className="rounded-md border border-[#2d1747] bg-black/20 p-4 text-sm text-slate-300">
            <div className="font-black text-white">{selectedFormat.displayName}</div>
            <div className="mt-1 text-slate-400">{selectedFormat.description}</div>
            <div className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-4 text-xs font-bold uppercase text-slate-500">
              <div>Familia: {familyLabelMap[selectedFormat.family] || selectedFormat.family}</div>
              <div>Minimo: {selectedFormat.minimumParticipants}</div>
              <div>Maximo: {selectedFormat.maximumParticipants || 'Sin limite'}</div>
              <div>Bracket auto: {selectedFormat.supportsBracketGeneration ? 'Si' : 'No'}</div>
              <div>Siembra ELO: {selectedFormat.supportsRankingSeeding ? 'Si' : 'No'}</div>
              <div>Bo default: {selectedFormat.defaultBestOf}</div>
              <div>Vueltas liga: {selectedFormat.roundRobinCycles || 1}</div>
              <div>Rondas swiss: {selectedFormat.swissRounds || 'Auto'}</div>
            </div>
          </div>
        )}

        <button className="mt-2 w-fit rounded-md bg-[#b65cff] px-4 py-2 font-black text-white transition-colors hover:bg-[#a855f7]">
          Crear torneo
        </button>

        {message && <div className="rounded-md border border-emerald-400/30 bg-emerald-500/10 px-3 py-2 text-sm font-bold text-emerald-300">{message}</div>}
        {error && <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">{error}</div>}
      </form>
    </div>
  )
}
