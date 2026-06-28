import React from 'react'
import Navbar from '../components/Navbar'

export default function MainLayout({ children }){
  return (
    <div className="app-shell min-h-screen bg-[#07070c] text-slate-100">
      <Navbar />
      <main className="page-enter mx-auto w-full max-w-6xl px-4 pb-10 pt-5 sm:px-6">{children}</main>
    </div>
  )
}
