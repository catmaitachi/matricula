import { useRef, useState, type FormEvent } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { ErroApi } from '../../api/tipos'
import { useAuth } from '../../auth/contexto'
import { BotaoEntrar } from './BotaoEntrar'
import { CampoEntrar } from './CampoEntrar'
import estilos from './Entrar.module.css'

interface Erros {
  numPessoa?: string
  senha?: string
  geral?: string
}

export function Entrar() {
  const { usuario, entrar } = useAuth()
  const local = useLocation()
  const [numPessoa, setNumPessoa] = useState('')
  const [senha, setSenha] = useState('')
  const [erros, setErros] = useState<Erros>({})
  const [enviando, setEnviando] = useState(false)
  const refNumPessoa = useRef<HTMLInputElement>(null)
  const refSenha = useRef<HTMLInputElement>(null)

  if (usuario) {
    const de = (local.state as { de?: unknown } | null)?.de
    const interno = typeof de === 'string' && de.startsWith('/') && !de.startsWith('//')
    return <Navigate to={interno ? de : '/'} replace />
  }

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando) return

    const faltas: Erros = {
      numPessoa: numPessoa.trim() ? undefined : 'Informe seu nº de pessoa.',
      senha: senha ? undefined : 'Informe sua senha.',
    }
    if (faltas.numPessoa || faltas.senha) {
      setErros(faltas)
      ;(faltas.numPessoa ? refNumPessoa : refSenha).current?.focus()
      return
    }

    setErros({})
    setEnviando(true)
    try {
      await entrar(numPessoa.trim(), senha)
    } catch (erro) {
      setErros({ geral: erro instanceof ErroApi ? erro.message : 'Algo deu errado. Tente de novo.' })
      setSenha('')
      refSenha.current?.focus()
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className={estilos.cena}>
      <title>Entrar · Matrícula</title>
      <div className={estilos.meta}>
        <span>Sistema acadêmico</span>
        <span>Laboratório de Desenvolvimento de Software</span>
      </div>
      <h1 className={estilos.titulo}>Matrícula</h1>
      <form className={estilos.form} onSubmit={aoEnviar} noValidate aria-label="Entrar">
        {erros.geral && (
          <p className={estilos.erroGeral} role="alert">
            {erros.geral}
          </p>
        )}
        <div className={estilos.campos}>
          <CampoEntrar
            ref={refNumPessoa}
            rotulo="Nº de pessoa"
            name="numPessoa"
            autoComplete="username"
            autoCapitalize="characters"
            spellCheck={false}
            required
            value={numPessoa}
            onChange={(e) => setNumPessoa(e.target.value)}
            erro={erros.numPessoa}
          />
          <CampoEntrar
            ref={refSenha}
            rotulo="Senha"
            name="senha"
            type="password"
            autoComplete="current-password"
            required
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            erro={erros.senha}
          />
          <BotaoEntrar type="submit" carregando={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </BotaoEntrar>
        </div>
      </form>
    </main>
  )
}
