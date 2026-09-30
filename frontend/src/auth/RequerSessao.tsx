import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './contexto'

export function RequerSessao({ children }: { children: ReactNode }) {
  const { usuario, verificando } = useAuth()
  const local = useLocation()

  if (verificando) return <p role="status">Verificando sua sessão…</p>
  if (!usuario) return <Navigate to="/entrar" replace state={{ de: local.pathname }} />
  return <>{children}</>
}
