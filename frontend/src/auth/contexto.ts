import { createContext, useContext } from 'react'
import type { Usuario } from '../api/tipos'

export interface Auth {
  usuario: Usuario | null
  /** verdadeiro enquanto se pergunta ao servidor se já existe uma sessão (ex.: ao recarregar a página) */
  verificando: boolean
  entrar(numPessoa: string, senha: string): Promise<Usuario>
  sair(): Promise<void>
}

export const AuthContexto = createContext<Auth | null>(null)

export function useAuth(): Auth {
  const auth = useContext(AuthContexto)
  if (!auth) throw new Error('useAuth precisa estar dentro do AuthProvider')
  return auth
}
