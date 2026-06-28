import React from 'react'
import { Link, useParams } from 'react-router-dom'
import { CheckCircle } from 'lucide-react'

export default function TournamentPaymentSuccess() {
  const { id } = useParams()

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center space-y-6 text-center">
      <div className="rounded-full bg-emerald-500/10 p-6">
        <CheckCircle className="h-16 w-16 text-emerald-400" />
      </div>
      
      <div className="space-y-2">
        <h1 className="text-3xl font-black text-white">¡Pago completado!</h1>
        <p className="max-w-md text-sm font-medium text-slate-400">
          Tu pago ha sido procesado por Stripe. Nuestro sistema web validating el pago en unos momentos y tu torneo se publicará automáticamente, abriendo las inscripciones.
        </p>
      </div>

      <Link
        to={`/tournaments/${id}`}
        className="rounded-md bg-[#38f8d4] px-6 py-3 font-black text-black hover:bg-[#32e0c0]"
      >
        Volver al torneo
      </Link>
    </div>
  )
}
