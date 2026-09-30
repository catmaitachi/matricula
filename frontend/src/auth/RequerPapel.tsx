import { Navigate, Outlet } from 'react-router-dom'
import type { Papel } from '../api/tipos'
import { useAuth } from './contexto'

/** Só deixa passar quem tem o papel; os demais voltam ao início. O servidor confere de novo em cada chamada. */
export function RequerPapel({ papel }: { papel: Papel }) {
  const { usuario } = useAuth()
  return usuario?.papel === papel ? <Outlet /> : <Navigate to="/" replace />
}
