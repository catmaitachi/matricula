import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './estilos/base.css'
import { Rotas } from './Rotas'
import { AuthProvider } from './auth/AuthProvider'
import { AvisoProvider } from './componentes/AvisoProvider'

const raiz = document.getElementById('root')
if (!raiz) throw new Error('Elemento #root não encontrado')

createRoot(raiz).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <AvisoProvider>
          <Rotas />
        </AvisoProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
