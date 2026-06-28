import React from 'react'

export default function StatCard({ title, value, subtitle, accent }){
  return (
    <div className="card interactive-card min-h-[110px]">
      <div className="eyebrow">{title}</div>
      <div className={`mt-2 text-3xl font-black leading-none ${accent || 'text-white'}`}>{value}</div>
      {subtitle && <div className="mt-2 text-xs font-medium text-slate-500">{subtitle}</div>}
    </div>
  )
}
