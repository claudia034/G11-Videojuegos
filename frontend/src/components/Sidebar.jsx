import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Home, List, PlusCircle, Award, Users, GitBranch } from 'lucide-react'

const NavItem = ({ to, icon: Icon, label }) => {
  const loc = useLocation()
  const active = loc.pathname.startsWith(to)
  return (
    <Link to={to} className={`flex items-center gap-3 p-3 rounded-md ${active ? 'bg-primary/25 text-white' : 'text-slate-300 hover:bg-white/5'}`}>
      <Icon className="w-5 h-5" />
      <span className="text-sm font-medium">{label}</span>
    </Link>
  )
}

export default function Sidebar(){
  return (
    <aside className="w-64 fixed left-0 top-0 bottom-0 p-6 bg-gradient-to-b from-[#07102a] to-[#041025] border-r border-white/5">
      <div className="flex items-center gap-3 mb-8">
        <div className="w-10 h-10 rounded-md bg-gradient-to-br from-primary to-cyana flex items-center justify-center text-black font-bold">GT</div>
        <div>
          <div className="text-white font-bold">GameTourney</div>
          <div className="text-xs text-slate-400">Panel Admin</div>
        </div>
      </div>

      <nav className="flex flex-col gap-2">
        <NavItem to="/dashboard" icon={Home} label="Dashboard" />
        <NavItem to="/tournaments" icon={List} label="Torneos" />
        <NavItem to="/tournaments/create" icon={PlusCircle} label="Crear Torneo" />
        <NavItem to="/ranking" icon={Award} label="Ranking" />
        <NavItem to="/tournaments" icon={GitBranch} label="Bracket" />
        <NavItem to="/profile/me" icon={Users} label="Perfil" />
      </nav>

      <div className="mt-auto text-xs text-slate-500 pt-6">v0.1 · Nexus GG</div>
    </aside>
  )
}
