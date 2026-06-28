import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './styles/index.css'

const storedTheme = localStorage.getItem('nexus-theme') || 'dark'
document.documentElement.classList.add(storedTheme === 'light' ? 'light' : 'dark')

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
)
