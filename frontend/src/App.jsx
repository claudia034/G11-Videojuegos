import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Tournaments from './pages/Tournaments'
import TournamentDetail from './pages/TournamentDetail'
import CreateTournament from './pages/CreateTournament'
import EditTournament from './pages/EditTournament'
import TournamentPaymentSuccess from './pages/TournamentPaymentSuccess'
import TournamentPaymentCancel from './pages/TournamentPaymentCancel'
import Ranking from './pages/Ranking'
import Reports from './pages/Reports'
import Bracket from './pages/Bracket'
import Spectator from './pages/Spectator'
import Profile from './pages/Profile'
import AdminProfile from './pages/AdminProfile'
import Notifications from './pages/Notifications'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/dashboard" element={<MainLayout><Dashboard /></MainLayout>} />
      <Route path="/tournaments" element={<MainLayout><Tournaments /></MainLayout>} />
      <Route path="/tournaments/:id" element={<MainLayout><TournamentDetail /></MainLayout>} />
      <Route path="/tournaments/:id/success" element={<MainLayout><TournamentPaymentSuccess /></MainLayout>} />
      <Route path="/tournaments/:id/cancel" element={<MainLayout><TournamentPaymentCancel /></MainLayout>} />
      <Route path="/tournaments/:id/edit" element={<MainLayout><EditTournament /></MainLayout>} />
      <Route path="/tournaments/create" element={<MainLayout><CreateTournament /></MainLayout>} />
      <Route path="/ranking" element={<MainLayout><Ranking /></MainLayout>} />
      <Route path="/reports" element={<MainLayout><Reports /></MainLayout>} />
      <Route path="/bracket/:id" element={<MainLayout><Bracket /></MainLayout>} />
      <Route path="/spectator/:id" element={<MainLayout><Spectator /></MainLayout>} />
      <Route path="/profile/:id" element={<MainLayout><Profile /></MainLayout>} />
      <Route path="/admin/profile" element={<MainLayout><AdminProfile /></MainLayout>} />
      <Route path="/notifications" element={<MainLayout><Notifications /></MainLayout>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
