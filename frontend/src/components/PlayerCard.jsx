import React from 'react'

export default function PlayerCard({ player }){
  return (
    <div className="card interactive-card flex items-center justify-between gap-4">
      <div className="flex min-w-0 items-center gap-4">
        <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-[#b65cff] to-[#38f8d4] text-sm font-black text-black">{player.name.slice(0, 2).toUpperCase()}</div>
        <div className="min-w-0">
          <div className="truncate font-black">{player.name}</div>
          <div className="text-sm font-medium text-slate-500">{player.rank || `ELO ${player.rating}`}</div>
        </div>
      </div>
      <div className="text-right">
        <div className="text-lg font-black text-[#38f8d4]">{player.rating}</div>
        <div className="text-[11px] font-bold uppercase text-slate-500">ELO</div>
        <div className="mt-2 text-sm font-black text-[#ff9f1c]">{player.virtualPoints || 0} pts</div>
      </div>
    </div>
  )
}
