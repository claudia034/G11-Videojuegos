import React, { useEffect, useState } from 'react'
import { Award } from 'lucide-react'
import { playerService } from '../services/playerService'
import PlayerCard from '../components/PlayerCard'
import SectionBlock from '../components/SectionBlock'

export default function Ranking(){
  const [players, setPlayers] = useState([])
  useEffect(()=>{ playerService.list().then(setPlayers) },[])

  const top3 = players.slice(0,3)

  return (
    <div className="space-y-5">
      <SectionBlock eyebrow="Ranking" title="Top jugadores" icon={Award}>
        <div className="grid gap-4 md:grid-cols-3">
          {top3.map(p=> (
            <div key={p.id} className="card interactive-card text-center">
              <div className="eyebrow">Top {top3.indexOf(p)+1}</div>
              <div className="mt-2 text-2xl font-black">{p.name}</div>
              <div className="mt-1 text-sm font-medium text-slate-500">{p.wins}W - {p.losses}L</div>
              <div className="mt-3 text-3xl font-black text-[#38f8d4]">{p.rating}</div>
            </div>
          ))}
        </div>
      </SectionBlock>

      <SectionBlock eyebrow="Clasificacion" title="Ranking global">
        <div className="grid gap-3">
          {players.map(p=> <PlayerCard key={p.id} player={p} />)}
        </div>
      </SectionBlock>
    </div>
  )
}
