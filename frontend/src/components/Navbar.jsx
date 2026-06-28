import React, { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Award, BarChart3, Bell, GitBranch, Home, LogOut, Menu, Shield, User, Workflow, X } from 'lucide-react'
import ThemeToggle from './ThemeToggle'
import { storage } from '../services/api'

const mainLinks = [
  { to: '/dashboard', label: 'Lobby', icon: Home },
  { to: '/tournaments', label: 'Torneos', icon: GitBranch },
  { to: '/ranking', label: 'Ranking', icon: Award },
  { to: '/reports', label: 'Reportes', icon: BarChart3 },
  { to: '/notifications', label: 'Notificaciones', icon: Bell }
]

export default function Navbar() {
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const sessionUser = storage.getUser()
  const role = localStorage.getItem('nexus-role') || sessionUser?.role?.toLowerCase() || 'player'
  const username = sessionUser?.email || localStorage.getItem('nexus-user') || 'Sin sesion'
  const isAdmin = role === 'admin'
  const isOrganizer = role === 'organizer'
  const initialsSource = sessionUser?.email || username
  const initials = initialsSource.slice(0, 2).toUpperCase()
  const closeMenu = () => setMenuOpen(false)

  useEffect(() => {
    document.body.classList.toggle('nav-menu-open', menuOpen)
    return () => {
      document.body.classList.remove('nav-menu-open')
    }
  }, [menuOpen])

  const logout = () => {
    localStorage.removeItem('nexus-role')
    localStorage.removeItem('nexus-user')
    localStorage.removeItem('nexus-user-data')
    localStorage.removeItem('nexus-token')
    localStorage.removeItem('nexus-refresh-token')
    setMenuOpen(false)
    navigate('/')
  }

  const linkClass = ({ isActive }) =>
    `nav-link group relative flex items-center gap-3 overflow-hidden rounded-xl border px-3 py-3 text-sm font-bold transition-colors ${
      isActive
        ? 'nav-link-active border-[#b65cff] bg-[#2a0f3d] text-white shadow-[0_0_28px_rgba(182,92,255,0.18)]'
        : 'nav-link-idle border-[#241338] bg-[#0a0a11] text-slate-400 hover:border-[#6f3bb2] hover:text-white'
    }`

  const menuOverlay = menuOpen && typeof document !== 'undefined'
    ? createPortal(
      <div className="nav-overlay fixed inset-0 z-[9999] isolate">
        <button type="button" aria-label="Cerrar menu" onClick={closeMenu} className="fixed inset-0 z-0 bg-black/80 backdrop-blur-sm" />
        <aside className="nav-drawer fixed left-0 top-0 z-10 flex h-screen w-[min(22rem,90vw)] flex-col overflow-hidden border-r border-[#3c1f5d] bg-[#090910] shadow-[28px_0_90px_rgba(0,0,0,0.55)]">
          <div className="flex h-16 items-center justify-between border-b border-[#241338] px-4">
            <Link to="/dashboard" onClick={closeMenu} className="brand-mark flex items-center gap-3">
              <Workflow className="h-6 w-6 text-[#ff9f1c]" />
              <span className="text-lg font-black tracking-[0.18em] text-[#b65cff]">NEXUS GG</span>
            </Link>
            <button type="button" aria-label="Cerrar menu" onClick={closeMenu} className="rounded-lg border border-[#2d1747] p-2 text-slate-300 hover:border-[#b65cff] hover:text-white">
              <X className="h-4 w-4" />
            </button>
          </div>

          <div className="nav-drawer-body min-h-0 flex-1 overflow-y-auto p-4">
            <div className="mb-5 rounded-xl border border-[#3b1f5d] bg-[linear-gradient(145deg,rgba(182,92,255,0.14),rgba(56,248,212,0.06)_52%,rgba(0,0,0,0.22))] p-4">
              <div className="eyebrow">Sesion actual</div>
              <div className="mt-3 flex items-center gap-3">
                <div className={`flex h-11 w-11 items-center justify-center rounded-full font-black text-black ${isAdmin || isOrganizer ? 'bg-gradient-to-br from-[#ff9f1c] to-[#b65cff]' : 'bg-gradient-to-br from-[#b65cff] to-[#38f8d4]'}`}>
                  {initials}
                </div>
                <div className="min-w-0">
                  <div className="font-black text-white">{username}</div>
                  <div className="text-xs font-bold uppercase text-slate-500">{isAdmin ? 'Administrador' : isOrganizer ? 'Organizador' : 'Jugador'}</div>
                </div>
              </div>
            </div>

            <div className="mb-2 px-1 text-[10px] font-black uppercase tracking-[0.2em] text-slate-600">Navegacion</div>
            <nav className="grid gap-2">
              {mainLinks.map(({ to, label, icon: Icon }) => (
                <NavLink key={to} to={to} onClick={closeMenu} className={linkClass}>
                  <span className="nav-link-icon flex h-8 w-8 items-center justify-center rounded-lg border border-white/5 bg-white/[0.03] text-slate-300 group-hover:text-white">
                    <Icon className="h-4 w-4" />
                  </span>
                  {label}
                </NavLink>
              ))}
            </nav>

            <div className="my-5 border-t border-[#241338]" />

            <div className="mb-2 px-1 text-[10px] font-black uppercase tracking-[0.2em] text-slate-600">Cuenta</div>
            <nav className="grid gap-2">
              {isAdmin || isOrganizer ? (
                <>
                  <NavLink to="/admin/profile" onClick={closeMenu} className={linkClass}>
                    <span className="nav-link-icon flex h-8 w-8 items-center justify-center rounded-lg border border-white/5 bg-white/[0.03] text-slate-300 group-hover:text-white">
                      <Shield className="h-4 w-4" />
                    </span>
                    Panel de gestion
                  </NavLink>
                  <NavLink to="/tournaments/create" onClick={closeMenu} className={linkClass}>
                    <span className="nav-link-icon flex h-8 w-8 items-center justify-center rounded-lg border border-white/5 bg-white/[0.03] text-slate-300 group-hover:text-white">
                      <GitBranch className="h-4 w-4" />
                    </span>
                    Crear Torneo
                  </NavLink>
                </>
              ) : (
                <NavLink to="/profile/me" onClick={closeMenu} className={linkClass}>
                  <span className="nav-link-icon flex h-8 w-8 items-center justify-center rounded-lg border border-white/5 bg-white/[0.03] text-slate-300 group-hover:text-white">
                    <User className="h-4 w-4" />
                  </span>
                  Mi Perfil
                </NavLink>
              )}
            </nav>
          </div>

          <div className="nav-drawer-footer shrink-0 border-t border-[#241338] bg-[#090910] p-4">
            <button onClick={logout} className="nav-logout-button flex w-full items-center gap-3 rounded-xl border border-[#2d1747] bg-[#0a0a11] px-3 py-3 text-left text-sm font-bold text-slate-400 transition-colors hover:border-red-400/40 hover:text-red-300">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/5 bg-white/[0.03]">
                <LogOut className="h-4 w-4" />
              </span>
              Cerrar sesion
            </button>
          </div>
        </aside>
      </div>,
      document.body
    )
    : null

  return (
    <>
      <header className="sticky top-0 z-[100] border-b border-[#2d1747] bg-[#0d0d14]/95 backdrop-blur">
        <div className="mx-auto flex h-14 w-full max-w-6xl items-center justify-between gap-3 px-4 sm:px-6">
          <div className="flex items-center gap-3">
            <button type="button" aria-label={menuOpen ? 'Cerrar menu' : 'Abrir menu'} aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)} className="rounded-lg border border-[#2d1747] p-2 text-slate-200 transition-colors hover:border-[#b65cff]">
              {menuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>

            <Link to="/dashboard" onClick={closeMenu} className="brand-mark flex shrink-0 items-center gap-3">
              <Workflow className="h-6 w-6 text-[#ff9f1c]" />
              <span className="text-lg font-black tracking-[0.18em] text-[#b65cff] sm:text-xl">NEXUS GG</span>
            </Link>
          </div>

          <div className="flex items-center gap-2">
            <ThemeToggle compact />
            <Link to={isAdmin || isOrganizer ? '/admin/profile' : '/profile/me'} className="flex items-center gap-2">
              <span className={`rounded-full border px-3 py-1 text-[11px] font-black uppercase tracking-[0.16em] ${isAdmin || isOrganizer ? 'border-[#ff9f1c]/35 bg-[#ff9f1c]/10 text-[#ffbf69]' : 'border-[#38f8d4]/35 bg-[#38f8d4]/10 text-[#38f8d4]'}`}>
                {isAdmin ? 'Admin' : isOrganizer ? 'Organizer' : 'Jugador'}
              </span>
              <span className="hidden text-sm font-semibold text-slate-500 sm:block">{username}</span>
            </Link>
          </div>
        </div>
      </header>
      {menuOverlay}
    </>
  )
}
