import { aoSessaoExpirar, requisitar } from './http'
import { ErroApi } from './tipos'

const fetchFalso = vi.fn<typeof fetch>()
const json = (corpo: unknown, status = 200) => new Response(JSON.stringify(corpo), { status })
const cabecalho = (chamada: number, nome: string) => {
  const init = fetchFalso.mock.calls[chamada]![1]
  return ((init?.headers ?? {}) as Record<string, string>)[nome]
}

beforeEach(() => {
  fetchFalso.mockReset()
  vi.stubGlobal('fetch', fetchFalso)
  document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/'
})
afterEach(() => vi.unstubAllGlobals())

describe('requisitar', () => {
  it('lê JSON com cookies da mesma origem e sem cabeçalho CSRF nem Authorization', async () => {
    fetchFalso.mockResolvedValue(json({ nome: 'Ana' }))

    await expect(requisitar('/auth/eu')).resolves.toEqual({ nome: 'Ana' })

    const [url, init] = fetchFalso.mock.calls[0]!
    expect(url).toBe('/api/auth/eu')
    expect(init?.credentials).toBe('same-origin')
    expect(init?.headers).not.toHaveProperty('X-XSRF-TOKEN')
    expect(init?.headers).not.toHaveProperty('Authorization')
  })

  it('devolve o token CSRF do cookie em toda escrita', async () => {
    document.cookie = 'XSRF-TOKEN=abc%3D123; path=/'
    fetchFalso.mockResolvedValue(json({}))

    await requisitar('/contas', { metodo: 'POST', json: { nome: 'Ana' } })

    expect(cabecalho(0, 'X-XSRF-TOKEN')).toBe('abc=123')
    expect(cabecalho(0, 'Content-Type')).toBe('application/json')
    expect(fetchFalso.mock.calls[0]![1]?.body).toBe('{"nome":"Ana"}')
  })

  it('busca o cookie CSRF antes da primeira escrita quando ainda não o tem', async () => {
    fetchFalso.mockImplementation(async (url) => {
      if (String(url).endsWith('/auth/eu')) {
        document.cookie = 'XSRF-TOKEN=novo; path=/'
        return json({ codigo: 'NAO_AUTENTICADO' }, 401)
      }
      return new Response(null, { status: 204 })
    })

    await requisitar('/auth/entrar', { metodo: 'POST', formulario: { numPessoa: 'ALU1', senha: 'x' } })

    expect(fetchFalso.mock.calls.map((c) => c[0])).toEqual(['/api/auth/eu', '/api/auth/entrar'])
    expect(cabecalho(1, 'X-XSRF-TOKEN')).toBe('novo')
    expect(cabecalho(1, 'Content-Type')).toBe('application/x-www-form-urlencoded')
    expect(String(fetchFalso.mock.calls[1]![1]?.body)).toBe('numPessoa=ALU1&senha=x')
  })

  it('trata 204 como sucesso sem corpo', async () => {
    document.cookie = 'XSRF-TOKEN=t; path=/'
    fetchFalso.mockResolvedValue(new Response(null, { status: 204 }))
    await expect(requisitar('/aluno/matriculas/1', { metodo: 'DELETE' })).resolves.toBeUndefined()
  })

  it('converte resposta de erro em ErroApi, com os erros por campo', async () => {
    fetchFalso.mockResolvedValue(
      json({ codigo: 'DADO_INVALIDO', mensagem: 'Confira os campos.', campos: { senha: 'tamanho inválido' } }, 400),
    )

    const erro = await requisitar('/x').catch((e: unknown) => e)

    expect(erro).toBeInstanceOf(ErroApi)
    expect(erro).toMatchObject({ status: 400, codigo: 'DADO_INVALIDO', campos: { senha: 'tamanho inválido' } })
  })

  it('usa uma mensagem genérica quando o erro não vem em JSON', async () => {
    fetchFalso.mockResolvedValue(new Response('<html>', { status: 502 }))
    await expect(requisitar('/x')).rejects.toMatchObject({ status: 502, codigo: 'ERRO', message: 'O servidor respondeu com erro 502.' })
  })

  it('trata falha de rede como SEM_CONEXAO', async () => {
    fetchFalso.mockRejectedValue(new TypeError('Failed to fetch'))
    await expect(requisitar('/x')).rejects.toMatchObject({ status: 0, codigo: 'SEM_CONEXAO' })
  })

  describe('sessão expirada', () => {
    it('avisa quando uma chamada comum recebe 401', async () => {
      const expirou = vi.fn()
      const parar = aoSessaoExpirar(expirou)
      fetchFalso.mockResolvedValue(json({ codigo: 'NAO_AUTENTICADO' }, 401))

      await requisitar('/aluno/matriculas').catch(() => undefined)

      expect(expirou).toHaveBeenCalledOnce()
      parar()
    })

    it.each(['/auth/eu', '/auth/entrar'])('não avisa quando %s recebe 401, que ali é resposta esperada', async (caminho) => {
      const expirou = vi.fn()
      const parar = aoSessaoExpirar(expirou)
      document.cookie = 'XSRF-TOKEN=t; path=/'
      fetchFalso.mockResolvedValue(json({ codigo: 'X' }, 401))

      await requisitar(caminho, { metodo: caminho === '/auth/eu' ? 'GET' : 'POST' }).catch(() => undefined)

      expect(expirou).not.toHaveBeenCalled()
      parar()
    })
  })
})
