import { useEffect, useState, type ReactNode } from 'react'
import { api } from '../api'
import { aoSessaoExpirar } from '../api/http'
import type { Usuario } from '../api/tipos'
import { AuthContexto, type Auth } from './contexto'

/**
 * Guarda só quem está logado, em memória. Não há token nem nada da sessão no JavaScript: ela vive num cookie
 * HttpOnly, e ao recarregar a página o app pergunta ao servidor quem é (`/auth/eu`).
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [verificando, setVerificando] = useState(true)

  useEffect(() => {
    let ativo = true
    api
      .eu()
      .then((u) => ativo && setUsuario(u))
      .catch(() => undefined) // sem sessão (401) ou sem rede: segue como visitante
      .finally(() => ativo && setVerificando(false))
    return () => {
      ativo = false
    }
  }, [])

  // qualquer chamada que receba 401 no meio do uso devolve o usuário à tela de entrada
  useEffect(() => aoSessaoExpirar(() => setUsuario(null)), [])

  const valor: Auth = {
    usuario,
    verificando,
    async entrar(numPessoa, senha) {
      await api.entrar(numPessoa, senha)
      const logado = await api.eu()
      setUsuario(logado)
      return logado
    },
    async sair() {
      try {
        await api.sair()
      } finally {
        setUsuario(null)
      }
    },
  }
  return <AuthContexto value={valor}>{children}</AuthContexto>
}
