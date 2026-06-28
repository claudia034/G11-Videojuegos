import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ShieldCheck, UserPlus, Workflow } from 'lucide-react'
import ThemeToggle from '../components/ThemeToggle'
import { authService } from '../services/authService'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: '',
    username: '',
    role: 'PLAYER',
    password: '',
    confirmPassword: ''
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const updateField = (field) => (event) => {
    setForm((current) => ({
      ...current,
      [field]: event.target.value
    }))
    setError('')
  }

  const submit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    if (form.password !== form.confirmPassword) {
      setLoading(false)
      setError('Las contrasenas no coinciden.')
      return
    }

    try {
      await authService.register({
        email: form.email.trim(),
        username: form.username.trim(),
        role: form.role,
        password: form.password,
        confirmPassword: form.confirmPassword
      })
      navigate('/dashboard')
    } catch (err) {
      setError(err.message || 'No se pudo crear la cuenta.')
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
              <div className="eyebrow">Registro</div>
              <h1 className="mt-2 max-w-sm text-4xl font-black leading-tight">Crea tu perfil y entra al lobby</h1>
              <p className="mt-4 max-w-sm text-sm font-medium leading-6 text-slate-500">
                Puedes registrarte como jugador, organizador o administrador segun el perfil que necesites.
              </p>
            </div>

            <div className="mt-8 rounded-lg border border-[#2d1747] bg-black/10 p-4">
              <div className="flex items-center gap-3">
                <ShieldCheck className="h-5 w-5 text-[#38f8d4]" />
                <div>
                  <div className="font-black">Requisitos de clave</div>
                  <div className="text-xs font-medium leading-5 text-slate-500">
                    Usa 8+ caracteres con mayuscula, minuscula, numero y simbolo.
                  </div>
                </div>
              </div>
            </div>
          </div>

          <form onSubmit={submit} className="grid gap-5 p-6 md:p-8">
            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Correo
              <input type="email" value={form.email} onChange={updateField('email')} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" placeholder="tuusuario@nexus.gg" required />
            </label>

            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Gamertag
              <input value={form.username} onChange={updateField('username')} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" placeholder="NuevoPlayer" minLength={3} maxLength={30} required />
            </label>

            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Rol
              <select value={form.role} onChange={updateField('role')} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]">
                <option value="PLAYER">Jugador</option>
                <option value="ORGANIZER">Organizador</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </label>

            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Contrasena
              <input type="password" value={form.password} onChange={updateField('password')} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" placeholder="Demo1234!" minLength={8} required />
            </label>

            <label className="grid gap-2 text-sm font-bold text-slate-300">
              Confirmar contrasena
              <input type="password" value={form.confirmPassword} onChange={updateField('confirmPassword')} className="rounded-md border border-[#2d1747] bg-black/25 p-3 text-white outline-none focus:border-[#b65cff]" placeholder="Repite la contrasena" minLength={8} required />
            </label>

            {error && (
              <div className="rounded-md border border-red-400/30 bg-red-500/10 px-3 py-2 text-sm font-bold text-red-300">
                {error}
              </div>
            )}

            <button disabled={loading} className="rounded-md bg-[#b65cff] px-4 py-3 font-black text-white transition-colors hover:bg-[#a855f7] disabled:cursor-not-allowed disabled:opacity-70">
              {loading ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>

            <div className="flex items-center justify-center gap-2 text-sm font-medium text-slate-400">
              <UserPlus className="h-4 w-4" />
              <span>¿Ya tienes cuenta?</span>
              <Link to="/login" className="font-black text-[#38f8d4] transition-colors hover:text-[#7ffbe5]">
                Inicia sesion
              </Link>
            </div>
          </form>
        </section>
      </div>
    </main>
  )
}
