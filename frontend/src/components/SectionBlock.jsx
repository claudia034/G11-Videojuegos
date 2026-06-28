import React from 'react'

export default function SectionBlock({ eyebrow, title, icon: Icon, action, children, className = '' }){
  return (
    <section className={`section-block reveal-section ${className}`}>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          {Icon && (
            <div className="rounded-lg border border-[#2d1747] bg-black/20 p-2 text-[#b65cff]">
              <Icon className="h-5 w-5" />
            </div>
          )}
          <div>
            {eyebrow && <div className="eyebrow">{eyebrow}</div>}
            <h2 className="mt-1 text-xl font-black tracking-wide text-slate-100">{title}</h2>
          </div>
        </div>
        {action}
      </div>
      {children}
    </section>
  )
}
