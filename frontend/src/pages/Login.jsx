import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Shield, User, Workflow } from 'lucide-react'
import ThemeToggle from '../components/ThemeToggle'
import { authService } from '../services/authService'

export default function Login(){
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      await authService.login(email.trim(), password)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message || 'Usuario o contrasena incorrectos.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app-shell min-h-screen bg-[#07070c] px-4 py-8 text-slate-100">
      <div className="mx-auto flex w-full max-w-5xl justify-end">
        <ThemeToggle />
      </div>
      <div className="mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-5xl items-center justify-center">
        <section className="page-enter grid w-full overflow-hidden rounded-xl border border-[#2d1747] bg-[#0d0d14] shadow-[0_24px_80px_rgba(0,0,0,0.45)] md:grid-cols-[0.95fr_1.05fr]">
          <div className="border-b border-[#2d1747] bg-black/20 p-6 md:border-b-0 md:border-r md:p-8">
            <div className="flex items-center gap-3">
              <Workflow className="h-8 w-8 text-[#ff9f1c]" />
              <div className="text-2xl font-black tracking-[0.18em] text-[#b65cff]">NEXUS GG</div>
            </div>

            <div className="mt-12">
              <div className="eyebrow">Acceso</div>
              <h1 className="mt-2 max-w-sm text-4xl font-black leading-tight">Inicia sesion para entrar al lobby</h1>
              <p className="mt-4 max-w-sm text-sm font-medium leading-6 text-slate-500">
                Usa tus credenciales reales y el sistema cargara tu perfil directamente desde el backend.
              </p>
            </div>
          </div>

          <form onSubmit={submit} className="grid gap-5 p-6 md:p-8">
            <div className="rounded-lg border border-[#2d1747] bg-black/10 p-4">
              <div className="eyebrow">Acceso seguro</div>
              <div className="mt-3 grid gap-3 sm:grid-cols-2">
                <div className="flex items-center gap-3">
                  <User className="h-5 w-5 text-[#38f8d4]" />
                  <div>
                    <div className="font-black">Jugador</div>
                    <div className="text-xs font-medium text-slate-500">Accede a torneos, ranking y perfil</div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Shield className="h-5 w-5 text-[#ff9f1c]" />
                  <div>
                    <div className="font-black">Administrador</div>
                    <div className="text-xs font-medium text-slate-500">Gestiona torneos y revisiones</div>
                  </div>
                </div>
              </div>
            </div>

            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Correo
              <input
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value)
                  setError('')
                }}
                className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                placeholder="correo@dominio.com"
              />
            </label>

            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Contrasena
              <input
                type="password"
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value)
                  setError('')
                }}
                className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]"
                placeholder="Tu contrasena"
              />
            </label>

            {error && (
              <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">
                {error}
              </div>
            )}

            <button disabled={loading} className="rounded-md bg-[#b65cff] px-4 py-3 font-black text-white transition-colors hover:bg-[#a855f7] disabled:cursor-not-allowed disabled:opacity-70">
              {loading ? 'Entrando...' : 'Iniciar sesion'}
            </button>

            <div className="text-center text-sm font-medium text-slate-400">
              <span>No tienes cuenta todavia? </span>
              <Link to="/register" className="font-black text-[#38f8d4] transition-colors hover:text-[#7ffbe5]">
                Crear cuenta
              </Link>
            </div>
          </form>
        </section>
      </div>
    </main>
  )
}
