import React from 'react'
import { Link, useParams } from 'react-router-dom'
import { XCircle } from 'lucide-react'

export default function TournamentPaymentCancel() {
  const { id } = useParams()

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center space-y-6 text-center">
      <div className="rounded-full bg-red-500/10 p-6">
        <XCircle className="h-16 w-16 text-red-400" />
      </div>
      
      <div className="space-y-2">
        <h1 className="text-3xl font-black text-white">Pago interrumpido</h1>
        <p className="max-w-md text-sm font-medium text-slate-400">
          El proceso de fondeo fue cancelado o no se pudo completar. Tu torneo sigue en estado DRAFT y no será publicado hasta que completes el pago.
        </p>
      </div>

      <Link
        to={`/tournaments/${id}`}
        className="rounded-md border border-[#2d1747] bg-black/20 px-6 py-3 font-black text-slate-300 hover:border-[#ff9f1c] hover:text-[#ffbf69]"
      >
        Volver al torneo y reintentar
      </Link>
    </div>
  )
}
