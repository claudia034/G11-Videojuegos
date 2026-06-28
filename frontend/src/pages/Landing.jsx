import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Activity, ArrowRight, Bell, CalendarDays, Crown, GitBranch, Play, ShieldCheck, Sparkles, Trophy, Users } from 'lucide-react'
import ThemeToggle from '../components/ThemeToggle'
import logo from '../assets/logo.svg'
import { tournamentService } from '../services/tournamentService'
import { playerService } from '../services/playerService'

const features = [
  { icon: GitBranch, title: 'Brackets vivos', text: 'Cruces, resultados y avances listos para seguir cada ronda.' },
  { icon: ShieldCheck, title: 'Panel admin', text: 'Gestiona torneos, reportes, pagos y aprobaciones desde un solo lugar.' },
  { icon: Users, title: 'Comunidad', text: 'Perfiles, ranking y notificaciones para mantener el lobby activo.' }
]

function ArenaPreview({ featuredTournament, featuredPlayers }){
  return (
    <div className="home-arena-preview" aria-hidden="true">
      <div className="home-preview-top">
        <span className="home-live-dot" />
        <span>{featuredTournament?.name || 'Torneos en plataforma'}</span>
        <strong>{featuredTournament?.status === 'IN_PROGRESS' ? 'LIVE' : 'OPEN'}</strong>
      </div>

      <div className="home-preview-grid">
        <div className="home-preview-panel home-preview-main">
          <div className="home-panel-header">
            <div>
              <span>Bracket</span>
              <strong>{featuredTournament?.format || 'Formato disponible'}</strong>
            </div>
            <Trophy className="h-5 w-5" />
          </div>

          <div className="home-mini-bracket">
            <div className="home-mini-round">
              <span>{featuredPlayers[0]?.name || 'Jugador 1'}</span>
              <span>{featuredPlayers[1]?.name || 'Jugador 2'}</span>
            </div>
            <div className="home-mini-connector" />
            <div className="home-mini-round home-mini-winner">
              <Crown className="h-4 w-4" />
              <span>{featuredPlayers[0]?.name || 'Ganador'}</span>
            </div>
            <div className="home-mini-connector" />
            <div className="home-mini-round">
              <span>{featuredPlayers[2]?.name || 'Jugador 3'}</span>
              <span>{featuredPlayers[3]?.name || 'Jugador 4'}</span>
            </div>
          </div>
        </div>

        <div className="home-preview-panel home-score-feed">
          <div className="home-panel-header">
            <div>
              <span>Ranking</span>
              <strong>Top actual</strong>
            </div>
            <Activity className="h-5 w-5" />
          </div>

          <div className="home-match-list">
            {featuredPlayers.slice(0, 3).map((player, index) => (
              <div key={player.id} className="home-match-row">
                <span>{player.name}</span>
                <small>ELO {player.rating}</small>
                <span>Top {index + 1}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="home-preview-panel home-side-card">
          <CalendarDays className="h-5 w-5" />
          <strong>{featuredTournament?.format || 'Formato'}</strong>
          <span>Modo del torneo</span>
        </div>

        <div className="home-preview-panel home-side-card">
          <Bell className="h-5 w-5" />
          <strong>{featuredTournament?.participants || 0}</strong>
          <span>Slots del torneo</span>
        </div>
      </div>
    </div>
  )
}

export default function Landing(){
  const [featuredTournament, setFeaturedTournament] = useState(null)
  const [featuredPlayers, setFeaturedPlayers] = useState([])

  useEffect(() => {
    tournamentService.list().then((tournaments) => {
      const featured = tournaments.find((tournament) => tournament.status === 'IN_PROGRESS')
        || tournaments.find((tournament) => tournament.status === 'REGISTRATION_OPEN')
        || tournaments[0]
      setFeaturedTournament(featured || null)
    }).catch(() => setFeaturedTournament(null))

    playerService.list().then((players) => {
      setFeaturedPlayers(players.slice(0, 4))
    }).catch(() => setFeaturedPlayers([]))
  }, [])

  return (
    <main className="home-page min-h-screen text-slate-100">
      <header className="home-nav">
        <Link to="/" className="home-brand">
          <img src={logo} alt="" />
          <span>NEXUS GG</span>
        </Link>

        <nav className="home-nav-actions">
          <ThemeToggle compact />
          <Link to="/login" className="home-login-link">Iniciar sesion</Link>
          <Link to="/register" className="home-signup-link">Crear cuenta</Link>
        </nav>
      </header>

      <section className="home-hero">
        <div className="home-copy page-enter">
          <div className="home-kicker">
            <Sparkles className="h-4 w-4" />
            Torneos, rankings y comunidad en una sola arena
          </div>

          <h1>Convierte cada torneo en una experiencia competitiva.</h1>
          <p>
            Diseña brackets, controla inscripciones, acompaña partidas en vivo y entrega a jugadores y admins un lobby que se siente activo desde el primer clic.
          </p>

          <div className="home-actions">
            <Link to="/register" className="home-primary-action">
              Comenzar ahora
              <ArrowRight className="h-5 w-5" />
            </Link>
            <Link to={featuredTournament ? `/bracket/${featuredTournament.id}` : '/tournaments'} className="home-secondary-action">
              <Play className="h-5 w-5" />
              Ver bracket actual
            </Link>
          </div>
        </div>

        <ArenaPreview featuredTournament={featuredTournament} featuredPlayers={featuredPlayers} />
      </section>

      <section className="home-feature-band" aria-label="Funciones principales">
        {features.map(({ icon: Icon, title, text }) => (
          <article key={title} className="home-feature-card">
            <Icon className="h-6 w-6" />
            <h2>{title}</h2>
            <p>{text}</p>
          </article>
        ))}
      </section>
    </main>
  )
}
