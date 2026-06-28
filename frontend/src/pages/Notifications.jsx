import React, { useEffect, useState } from 'react'
import { Bell, Radio } from 'lucide-react'
import SectionBlock from '../components/SectionBlock'
import { notificationService } from '../services/notificationService'

export default function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [live, setLive] = useState(false)

  useEffect(() => {
    notificationService.listUnread().then(setNotifications).catch(() => setNotifications([]))

    const stream = notificationService.stream()
    stream.onopen = () => setLive(true)
    stream.onerror = () => setLive(false)
    stream.addEventListener('notification', (event) => {
      try {
        const payload = JSON.parse(event.data)
        setNotifications((current) => [
          {
            ...payload,
            accent: payload.accent || 'text-[#38f8d4]'
          },
          ...current.filter((notification) => notification.id !== payload.id)
        ])
      } catch {
        setLive(false)
      }
    })

    return () => {
      stream.close()
    }
  }, [])

  const markAsRead = async (id) => {
    await notificationService.markAsRead(id)
    setNotifications((current) => current.filter((notification) => notification.id !== id))
  }

  return (
    <div className="space-y-4">
      <SectionBlock
        eyebrow="Centro"
        title="Notificaciones"
        icon={Bell}
        action={
          <div className={`inline-flex items-center gap-2 rounded-md border px-3 py-2 text-xs font-black uppercase ${live ? 'border-emerald-400/30 bg-emerald-500/10 text-emerald-300' : 'border-[#2d1747] bg-black/20 text-slate-400'}`}>
            <Radio className="h-4 w-4" />
            {live ? 'En vivo' : 'Sincronizando'}
          </div>
        }
      >
        {notifications.length ? (
          <div className="grid gap-3">
            {notifications.map(({ id, title, message, accent, createdAt }) => (
              <article key={id} className="card flex items-start justify-between gap-4">
                <div className="flex items-start gap-4">
                  <div className="rounded-lg border border-[#2d1747] bg-black/20 p-3">
                    <Bell className={`h-5 w-5 ${accent}`} />
                  </div>
                  <div>
                    <h2 className="font-black">{title}</h2>
                    <p className="mt-1 text-sm font-medium text-slate-500">{message}</p>
                    <div className="mt-2 text-xs font-bold uppercase text-slate-600">
                      {createdAt
                        ? new Intl.DateTimeFormat('es-SV', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(createdAt))
                        : 'Reciente'}
                    </div>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => markAsRead(id)}
                  className="rounded-md border border-[#2d1747] px-3 py-2 text-xs font-black uppercase text-slate-300 transition-colors hover:border-[#38f8d4] hover:text-[#38f8d4]"
                >
                  Marcar leida
                </button>
              </article>
            ))}
          </div>
        ) : (
          <div className="card text-sm font-medium text-slate-400">
            No tienes notificaciones pendientes.
          </div>
        )}
      </SectionBlock>
    </div>
  )
}
