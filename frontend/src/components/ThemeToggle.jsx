import React, { useEffect, useState } from 'react'
import { Moon, Sun } from 'lucide-react'

const THEME_KEY = 'nexus-theme'

const getStoredTheme = () => {
  if (typeof window === 'undefined') return 'dark'
  return localStorage.getItem(THEME_KEY) || 'dark'
}

const applyTheme = (theme) => {
  document.documentElement.classList.toggle('light', theme === 'light')
  document.documentElement.classList.toggle('dark', theme === 'dark')
}

export default function ThemeToggle({ compact = false }){
  const [theme, setTheme] = useState(getStoredTheme)
  const isLight = theme === 'light'

  useEffect(() => {
    applyTheme(theme)
    localStorage.setItem(THEME_KEY, theme)
  }, [theme])

  const toggleTheme = () => {
    setTheme((currentTheme) => currentTheme === 'dark' ? 'light' : 'dark')
  }

  return (
    <button
      type="button"
      aria-label={isLight ? 'Cambiar a modo oscuro' : 'Cambiar a modo claro'}
      title={isLight ? 'Modo oscuro' : 'Modo claro'}
      onClick={toggleTheme}
      className="theme-toggle inline-flex h-10 items-center gap-2 rounded-lg border border-[#2d1747] bg-black/20 px-3 text-sm font-black text-slate-200 transition-colors hover:border-[#b65cff] hover:text-white"
    >
      {isLight ? <Moon className="h-4 w-4" /> : <Sun className="h-4 w-4" />}
      {!compact && <span>{isLight ? 'Oscuro' : 'Claro'}</span>}
    </button>
  )
}
