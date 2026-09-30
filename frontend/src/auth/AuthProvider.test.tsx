import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { api } from '../api'
import { aoSessaoExpirar } from '../api/http'
import { ErroApi } from '../api/tipos'
import { SESSAO_ALUNO } from '../testes/fixtures'
import { AuthProvider } from './AuthProvider'
import { useAuth } from './contexto'

vi.mock('../api', () => ({ api: { entrar: vi.fn(), eu: vi.fn(), sair: vi.fn() } }))
vi.mock('../api/http', () => ({ aoSessaoExpirar: vi.fn(() => () => undefined) }))
const { entrar, eu, sair } = vi.mocked(api)

function Consumidor() {
  const auth = useAuth()
  if (auth.verificando) return <p>verificando</p>
  return (
    <>
      <p>{auth.usuario ? `logado: ${auth.usuario.nome}` : 'visitante'}</p>
      <button onClick={() => void auth.entrar('ALU999', 'x')}>entrar</button>
      <button onClick={() => void auth.sair()}>sair</button>
    </>
  )
}

const abrir = () =>
  render(
    <AuthProvider>
      <Consumidor />
    </AuthProvider>,
  )

beforeEach(() => {
  vi.clearAllMocks()
  sessionStorage.clear()
  localStorage.clear()
})

describe('AuthProvider', () => {
  it('pergunta ao servidor quem está logado ao carregar', async () => {
    eu.mockResolvedValue(SESSAO_ALUNO.usuario)

    abrir()

    expect(screen.getByText('verificando')).toBeInTheDocument()
    expect(await screen.findByText('logado: João Pedro')).toBeInTheDocument()
  })

  it('segue como visitante quando não há sessão', async () => {
    eu.mockRejectedValue(new ErroApi(401, 'NAO_AUTENTICADO', 'sem sessão'))
    abrir()
    expect(await screen.findByText('visitante')).toBeInTheDocument()
  })

  it('segue como visitante quando o servidor não responde', async () => {
    eu.mockRejectedValue(new ErroApi(0, 'SEM_CONEXAO', 'sem rede'))
    abrir()
    expect(await screen.findByText('visitante')).toBeInTheDocument()
  })

  it('entra e depois confirma quem é no servidor', async () => {
    eu.mockRejectedValueOnce(new ErroApi(401, 'NAO_AUTENTICADO', 'sem sessão')).mockResolvedValueOnce(SESSAO_ALUNO.usuario)
    entrar.mockResolvedValue(undefined)
    abrir()
    await screen.findByText('visitante')

    await userEvent.click(screen.getByRole('button', { name: 'entrar' }))

    expect(entrar).toHaveBeenCalledWith('ALU999', 'x')
    expect(await screen.findByText('logado: João Pedro')).toBeInTheDocument()
  })

  it('sair volta a visitante mesmo que o servidor falhe', async () => {
    eu.mockResolvedValue(SESSAO_ALUNO.usuario)
    sair.mockRejectedValue(new ErroApi(0, 'SEM_CONEXAO', 'sem rede'))
    abrir()
    await screen.findByText('logado: João Pedro')

    await userEvent.click(screen.getByRole('button', { name: 'sair' }))

    expect(await screen.findByText('visitante')).toBeInTheDocument()
  })

  it('volta a visitante quando o servidor avisa que a sessão expirou', async () => {
    eu.mockResolvedValue(SESSAO_ALUNO.usuario)
    abrir()
    await screen.findByText('logado: João Pedro')

    const expirar = vi.mocked(aoSessaoExpirar).mock.calls.at(-1)![0]
    act(() => expirar())

    expect(await screen.findByText('visitante')).toBeInTheDocument()
  })

  it('não grava nada da sessão no armazenamento do navegador', async () => {
    eu.mockResolvedValue(SESSAO_ALUNO.usuario)
    abrir()
    await screen.findByText('logado: João Pedro')
    expect(sessionStorage.length).toBe(0)
    expect(localStorage.length).toBe(0)
  })
})
