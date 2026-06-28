import React, { useEffect, useState } from 'react'

const formatDateTime = (value) => {
  if (!value) return 'Sin programar'
  return new Intl.DateTimeFormat('es-SV', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

const statusLabel = {
  SCHEDULED: 'Programado',
  IN_PROGRESS: 'En juego',
  RESULT_SUBMITTED: 'Resultado enviado',
  DISPUTED: 'En disputa',
  COMPLETED: 'Finalizado',
  BYE: 'BYE'
}

const statusTone = {
  SCHEDULED: 'text-slate-400',
  IN_PROGRESS: 'text-[#38f8d4]',
  RESULT_SUBMITTED: 'text-[#ffbf69]',
  DISPUTED: 'text-red-300',
  COMPLETED: 'text-[#b65cff]',
  BYE: 'text-slate-500'
}

const escapeXml = (value = '') =>
  String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;')

const normalizeRounds = (rounds = []) =>
  rounds.map((round, roundIndex) => ({
    ...round,
    matches: (round.matches || []).map((match, matchIndex) => ({
      ...match,
      uiRoundIndex: roundIndex,
      uiMatchIndex: matchIndex
    }))
  }))

const buildBracketLayout = (rounds = []) => {
  const normalizedRounds = normalizeRounds(rounds)
  const columnWidth = 240
  const nodeWidth = 150
  const leftPadding = 110
  const topPadding = 92
  const bottomPadding = 90
  const baseGap = 90
  const nodeHeight = 62
  const matchMap = new Map()

  normalizedRounds.forEach((round) => {
    round.matches.forEach((match) => {
      matchMap.set(match.id, match)
    })
  })

  const roundSpacing = normalizedRounds.map((round, roundIndex) => {
    const laterMatches = normalizedRounds.slice(roundIndex + 1).reduce((max, candidate) => (
      Math.max(max, candidate.matches?.length || 0)
    ), 0)
    return laterMatches > 0 ? Math.max(120, baseGap * Math.pow(2, roundIndex)) : 120
  })

  const nodes = normalizedRounds.flatMap((round, roundIndex) => {
    const spacing = roundSpacing[roundIndex]
    return round.matches.map((match, matchIndex) => {
      const x = leftPadding + roundIndex * columnWidth
      const y = topPadding + matchIndex * spacing + nodeHeight / 2
      return {
        ...match,
        roundName: round.name,
        roundNumber: round.roundNumber,
        roundStatus: round.status,
        x,
        y,
        width: nodeWidth,
        height: nodeHeight
      }
    })
  })

  const nodeMap = new Map(nodes.map((node) => [node.id, node]))
  const lines = nodes
    .filter((node) => node.nextMatchId && nodeMap.has(node.nextMatchId))
    .map((node) => {
      const nextNode = nodeMap.get(node.nextMatchId)
      const startX = node.x + node.width / 2
      const startY = node.y
      const endX = nextNode.x - nextNode.width / 2
      const endY = nextNode.y
      const midpointX = startX + (endX - startX) / 2

      return {
        id: `${node.id}-${nextNode.id}`,
        d: `M ${startX} ${startY} L ${midpointX} ${startY} L ${midpointX} ${endY} L ${endX} ${endY}`
      }
    })

  const width = Math.max(980, leftPadding * 2 + normalizedRounds.length * columnWidth)
  const height = Math.max(430, topPadding + nodes.reduce((max, node) => Math.max(max, node.y), 0) + bottomPadding)

  const championNode = [...nodes].reverse().find((node) => node.winnerName)
  const champion = championNode ? {
    name: championNode.winnerName,
    x: championNode.x + columnWidth * 0.72,
    y: championNode.y
  } : null

  return { rounds: normalizedRounds, nodes, lines, width, height, champion }
}

const buildBracketSvg = (title, rounds = []) => {
  const layout = buildBracketLayout(rounds)
  const parts = [
    `<svg xmlns="http://www.w3.org/2000/svg" width="${layout.width}" height="${layout.height}">`,
    '<defs><linearGradient id="lineGradient" x1="0%" x2="100%" y1="0%" y2="0%"><stop offset="0%" stop-color="#38f8d4"/><stop offset="100%" stop-color="#b65cff"/></linearGradient></defs>',
    '<rect width="100%" height="100%" fill="#0a0a11" />',
    `<text x="40" y="44" fill="#ffffff" font-size="24" font-family="Arial" font-weight="700">${escapeXml(title)}</text>`
  ]

  layout.rounds.forEach((round, roundIndex) => {
    const x = 110 + roundIndex * 240
    parts.push(`<text x="${x}" y="76" fill="#94a3b8" font-size="12" font-family="Arial" font-weight="700">${escapeXml(round.name || `Ronda ${round.roundNumber}`)}</text>`)
  })

  layout.lines.forEach((line) => {
    parts.push(`<path d="${line.d}" fill="none" stroke="url(#lineGradient)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />`)
  })

  layout.nodes.forEach((node) => {
    const winner1 = node.winnerRegistrationId === node.participant1RegistrationId
    const winner2 = node.winnerRegistrationId === node.participant2RegistrationId
    const x = node.x - node.width / 2
    const y = node.y - node.height / 2
    parts.push(`<rect x="${x}" y="${y}" rx="12" ry="12" width="${node.width}" height="${node.height}" fill="#131321" stroke="${node.winnerName ? '#38f8d4' : '#3b1f5d'}" />`)
    parts.push(`<text x="${x + 12}" y="${y + 24}" fill="${winner1 ? '#38f8d4' : '#e2e8f0'}" font-size="12" font-family="Arial" font-weight="700">${escapeXml(node.participant1Name || 'BYE')}</text>`)
    parts.push(`<text x="${x + 12}" y="${y + 44}" fill="${winner2 ? '#38f8d4' : '#e2e8f0'}" font-size="12" font-family="Arial" font-weight="700">${escapeXml(node.participant2Name || 'BYE')}</text>`)
  })

  if (layout.champion) {
    parts.push(`<rect x="${layout.champion.x - 58}" y="${layout.champion.y - 26}" rx="12" ry="12" width="116" height="52" fill="#251326" stroke="#ff9f1c" />`)
    parts.push(`<text x="${layout.champion.x}" y="${layout.champion.y - 6}" fill="#ffbf69" font-size="10" font-family="Arial" font-weight="700" text-anchor="middle">CAMPEON</text>`)
    parts.push(`<text x="${layout.champion.x}" y="${layout.champion.y + 12}" fill="#ffffff" font-size="13" font-family="Arial" font-weight="700" text-anchor="middle">${escapeXml(layout.champion.name)}</text>`)
  }

  parts.push('</svg>')
  return parts.join('')
}

export default function BracketBoard({ tournamentName, rounds = [], readonly = false }) {
  const layout = buildBracketLayout(rounds)
  const [selectedMatchId, setSelectedMatchId] = useState(null)

  useEffect(() => {
    const preferredMatch = layout.nodes.find((node) => node.status === 'IN_PROGRESS')
      || layout.nodes.find((node) => node.status === 'RESULT_SUBMITTED')
      || layout.nodes.find((node) => node.status === 'SCHEDULED')
      || layout.nodes[0]

    setSelectedMatchId(preferredMatch?.id || null)
  }, [rounds])

  const selectedMatch = layout.nodes.find((node) => node.id === selectedMatchId) || null

  const exportSvg = () => {
    const svg = buildBracketSvg(tournamentName, rounds)
    const blob = new Blob([svg], { type: 'image/svg+xml;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${(tournamentName || 'bracket').replace(/\s+/g, '-').toLowerCase()}.svg`
    link.click()
    URL.revokeObjectURL(url)
  }

  const exportPdf = () => {
    const printable = window.open('', '_blank', 'width=1280,height=900')
    if (!printable) return
    printable.document.write(`
      <html>
      <head>
        <title>${tournamentName}</title>
        <style>
          body { font-family: Arial, sans-serif; background: #ffffff; color: #111827; padding: 24px; }
          h1 { margin-bottom: 20px; }
          .round { margin-bottom: 24px; }
          .match { border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; margin: 8px 0; }
          .meta { color: #64748b; font-size: 12px; }
        </style>
      </head>
      <body>
        <h1>${tournamentName}</h1>
        ${rounds.map((round) => `
          <section class="round">
            <h2>${round.name || `Ronda ${round.roundNumber}`}</h2>
            ${(round.matches || []).map((match) => `
              <div class="match">
                <div><strong>${match.participant1Name}</strong> vs <strong>${match.participant2Name}</strong></div>
                <div class="meta">${statusLabel[match.status] || match.status} • ${formatDateTime(match.scheduledAt)}</div>
              </div>
            `).join('')}
          </section>
        `).join('')}
      </body>
      </html>
    `)
    printable.document.close()
    printable.focus()
    printable.print()
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <div className="eyebrow">{readonly ? 'Modo espectador' : 'Bracket en vivo'}</div>
          <h3 className="mt-1 text-lg font-black">{tournamentName}</h3>
          <div className="mt-2 text-xs font-bold uppercase text-slate-500">
            {readonly ? 'Toca una partida para ver su detalle en vivo' : 'Selecciona cualquier cruce para inspeccionarlo'}
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          <button type="button" onClick={exportSvg} className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#38f8d4] hover:text-[#38f8d4]">
            Exportar imagen
          </button>
          <button type="button" onClick={exportPdf} className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69]">
            Exportar PDF
          </button>
        </div>
      </div>

      {rounds.length ? (
        <>
          <div className="bracket-viewport rounded-2xl border border-[#2d1747]">
            <div className="bracket-canvas" style={{ width: `${layout.width}px`, height: `${layout.height}px`, minWidth: `${layout.width}px` }}>
              <svg className="bracket-lines" width={layout.width} height={layout.height} viewBox={`0 0 ${layout.width} ${layout.height}`}>
                <defs>
                  <linearGradient id="bracketLineLeft" x1="0%" x2="100%" y1="0%" y2="0%">
                    <stop offset="0%" stopColor="#38f8d4" />
                    <stop offset="100%" stopColor="#b65cff" />
                  </linearGradient>
                  <linearGradient id="bracketLineRight" x1="0%" x2="100%" y1="0%" y2="0%">
                    <stop offset="0%" stopColor="#b65cff" />
                    <stop offset="100%" stopColor="#ff9f1c" />
                  </linearGradient>
                </defs>
                {layout.lines.map((line, index) => (
                  <path
                    key={line.id}
                    d={line.d}
                    className={`bracket-line ${index === layout.lines.length - 1 ? 'bracket-line-right bracket-line-final' : ''}`}
                    style={{ animationDelay: `${index * 80}ms` }}
                  />
                ))}
              </svg>

              {layout.rounds.map((round, roundIndex) => (
                <div
                  key={round.id}
                  className="bracket-round-label"
                  style={{ left: `${110 + roundIndex * 240}px` }}
                >
                  {round.name || `Ronda ${round.roundNumber}`}
                </div>
              ))}

              {layout.nodes.map((node, index) => {
                const playerOneWinner = node.winnerRegistrationId === node.participant1RegistrationId
                const playerTwoWinner = node.winnerRegistrationId === node.participant2RegistrationId
                return (
                  <div
                    key={node.id}
                    className={`bracket-node cursor-pointer ${node.winnerName ? 'bracket-node-winner' : ''} ${selectedMatchId === node.id ? 'ring-2 ring-[#ff9f1c] ring-offset-2 ring-offset-[#080810]' : ''}`}
                    style={{
                      left: `${node.x}px`,
                      top: `${node.y}px`,
                      width: `${node.width}px`,
                      minHeight: `${node.height}px`,
                      animationDelay: `${120 + index * 60}ms`
                    }}
                    onClick={() => setSelectedMatchId(node.id)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault()
                        setSelectedMatchId(node.id)
                      }
                    }}
                  >
                    <div className={`bracket-node-player ${playerOneWinner ? 'bracket-node-player-winner' : ''}`}>
                      {node.participant1Name || 'BYE'}
                    </div>
                    <div className={`bracket-node-player ${playerTwoWinner ? 'bracket-node-player-winner' : ''}`}>
                      {node.participant2Name || 'BYE'}
                    </div>
                  </div>
                )
              })}

              {layout.champion && (
                <div className="bracket-center" style={{ left: `${layout.champion.x}px`, top: `${layout.champion.y}px` }}>
                  <div className="eyebrow text-[#ffbf69]">Campeon</div>
                  <div className="bracket-champion">{layout.champion.name}</div>
                </div>
              )}
            </div>
          </div>

          {selectedMatch && (
            <section className="grid gap-4 lg:grid-cols-[1.2fr_0.8fr]">
              <article className="card">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <div className="eyebrow">Partida seleccionada</div>
                    <div className="mt-1 text-xl font-black text-white">
                      {selectedMatch.participant1Name} vs {selectedMatch.participant2Name}
                    </div>
                    <div className="mt-2 text-sm font-medium text-slate-400">
                      {selectedMatch.roundName} • Bo{selectedMatch.bestOf || 1}
                    </div>
                  </div>
                  <div className={`rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase ${statusTone[selectedMatch.status] || 'text-slate-400'}`}>
                    {statusLabel[selectedMatch.status] || selectedMatch.status}
                  </div>
                </div>

                <div className="mt-4 grid gap-3 md:grid-cols-2">
                  <div className={`rounded-md border p-3 ${selectedMatch.winnerRegistrationId === selectedMatch.participant1RegistrationId ? 'border-[#38f8d4]/40 bg-[#38f8d4]/10' : 'border-[#2d1747] bg-black/20'}`}>
                    <div className="text-xs font-bold uppercase text-slate-500">Jugador 1</div>
                    <div className="mt-1 text-lg font-black text-white">{selectedMatch.participant1Name}</div>
                    <div className="mt-2 text-xs font-bold uppercase text-slate-500">
                      {selectedMatch.winnerRegistrationId === selectedMatch.participant1RegistrationId ? 'Ganando / gano la serie' : 'A la espera'}
                    </div>
                  </div>
                  <div className={`rounded-md border p-3 ${selectedMatch.winnerRegistrationId === selectedMatch.participant2RegistrationId ? 'border-[#38f8d4]/40 bg-[#38f8d4]/10' : 'border-[#2d1747] bg-black/20'}`}>
                    <div className="text-xs font-bold uppercase text-slate-500">Jugador 2</div>
                    <div className="mt-1 text-lg font-black text-white">{selectedMatch.participant2Name}</div>
                    <div className="mt-2 text-xs font-bold uppercase text-slate-500">
                      {selectedMatch.winnerRegistrationId === selectedMatch.participant2RegistrationId ? 'Ganando / gano la serie' : 'A la espera'}
                    </div>
                  </div>
                </div>
              </article>

              <article className="card">
                <div className="eyebrow">Lectura rapida</div>
                <div className="mt-4 grid gap-3">
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Horario</div>
                    <div className="mt-1 text-sm font-black text-white">{formatDateTime(selectedMatch.scheduledAt)}</div>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Siguiente paso</div>
                    <div className="mt-1 text-sm font-medium text-slate-300">
                      {selectedMatch.status === 'IN_PROGRESS'
                        ? 'La partida esta en juego y el resultado puede cambiar en cualquier momento.'
                        : selectedMatch.status === 'RESULT_SUBMITTED'
                          ? 'Ya hay resultado enviado. Falta confirmacion o una posible disputa.'
                          : selectedMatch.status === 'DISPUTED'
                            ? 'El encuentro esta congelado mientras el admin revisa la disputa.'
                            : selectedMatch.status === 'COMPLETED'
                              ? 'La serie ya se cerro y el bracket avanzo con ese ganador.'
                              : 'La partida aun no inicia o esta pendiente de programacion.'}
                    </div>
                  </div>
                  <div className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                    <div className="text-xs font-bold uppercase text-slate-500">Avance</div>
                    <div className="mt-1 text-sm font-medium text-slate-300">
                      {selectedMatch.nextMatchId ? `El ganador avanzara a la partida #${selectedMatch.nextMatchId}.` : 'Esta partida define al campeon o cierra la ruta visible del bracket.'}
                    </div>
                  </div>
                </div>
              </article>
            </section>
          )}

          <div className="grid gap-4 lg:grid-cols-3">
            {layout.rounds.map((round) => (
              <article key={round.id} className="card">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="eyebrow">Ronda {round.roundNumber}</div>
                    <h4 className="mt-1 text-lg font-black">{round.name}</h4>
                  </div>
                  <div className="rounded-md border border-[#2d1747] px-2 py-1 text-[10px] font-black uppercase text-slate-400">
                    {round.status}
                  </div>
                </div>

                <div className="mt-2 text-xs font-medium text-slate-500">
                  {formatDateTime(round.scheduledStart)}
                </div>

                <div className="mt-4 grid gap-3">
                  {(round.matches || []).map((match) => {
                    const playerOneWinner = match.winnerRegistrationId === match.participant1RegistrationId
                    const playerTwoWinner = match.winnerRegistrationId === match.participant2RegistrationId
                    return (
                      <div key={match.id} className="rounded-md border border-[#2d1747] bg-black/20 p-3">
                        <div className={`font-black ${playerOneWinner ? 'text-[#38f8d4]' : 'text-white'}`}>
                          {match.participant1Name}
                        </div>
                        <div className={`mt-1 font-black ${playerTwoWinner ? 'text-[#38f8d4]' : 'text-white'}`}>
                          {match.participant2Name}
                        </div>
                        <div className={`mt-2 text-[11px] font-bold uppercase ${statusTone[match.status] || 'text-slate-500'}`}>
                          {statusLabel[match.status] || match.status} • Bo{match.bestOf || 1}
                        </div>
                        <div className="mt-1 text-[11px] font-medium text-slate-500">
                          {formatDateTime(match.scheduledAt)}
                        </div>
                      </div>
                    )
                  })}
                </div>
              </article>
            ))}
          </div>
        </>
      ) : (
        <div className="card text-sm font-medium text-slate-400">
          Este torneo todavia no tiene bracket generado.
        </div>
      )}
    </div>
  )
}
