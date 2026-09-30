import { ErroApi } from './tipos'

const SEM_CONEXAO = 'Não foi possível falar com o servidor. Tente de novo em instantes.'
/** Caminhos em que um 401 é resposta esperada (sessão ainda não existe), não sessão expirada. */
const SEM_SESSAO = ['/auth/entrar', '/auth/eu']

type Metodo = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface Opcoes {
  metodo?: Metodo
  json?: unknown
  formulario?: Record<string, string>
}

interface CorpoErro {
  codigo?: string
  mensagem?: string
  campos?: Record<string, string>
}

let aoExpirar: () => void = () => {}

/** Registra o que fazer quando o servidor diz que a sessão acabou (a tela volta para a entrada). */
export function aoSessaoExpirar(acao: () => void) {
  aoExpirar = acao
  return () => {
    aoExpirar = () => {}
  }
}

// O token CSRF é um cookie legível de propósito: o app o devolve num cabeçalho, e um site de fora não consegue.
// A sessão em si fica num cookie HttpOnly que o JavaScript nem enxerga.
function tokenCsrf(): string | undefined {
  const achado = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)
  return achado?.[1] ? decodeURIComponent(achado[1]) : undefined
}

async function enviar(caminho: string, metodo: Metodo, corpo?: BodyInit, tipo?: string): Promise<Response> {
  const cabecalhos: Record<string, string> = { Accept: 'application/json' }
  if (tipo) cabecalhos['Content-Type'] = tipo
  if (metodo !== 'GET') {
    const token = tokenCsrf()
    if (token) cabecalhos['X-XSRF-TOKEN'] = token
  }
  try {
    return await fetch(`/api${caminho}`, { method: metodo, headers: cabecalhos, body: corpo, credentials: 'same-origin' })
  } catch {
    throw new ErroApi(0, 'SEM_CONEXAO', SEM_CONEXAO)
  }
}

export async function requisitar<T>(caminho: string, opcoes: Opcoes = {}): Promise<T> {
  const { metodo = 'GET', json, formulario } = opcoes
  // primeira escrita antes de qualquer leitura (ex.: recarregou direto na tela de entrada): busca o cookie CSRF
  if (metodo !== 'GET' && !tokenCsrf()) await enviar('/auth/eu', 'GET').catch(() => undefined)

  const resposta =
    formulario !== undefined
      ? await enviar(caminho, metodo, new URLSearchParams(formulario), 'application/x-www-form-urlencoded')
      : json !== undefined
        ? await enviar(caminho, metodo, JSON.stringify(json), 'application/json')
        : await enviar(caminho, metodo)

  if (!resposta.ok) {
    const erro = (await resposta.json().catch(() => ({}))) as CorpoErro
    if (resposta.status === 401 && !SEM_SESSAO.includes(caminho)) aoExpirar()
    throw new ErroApi(
      resposta.status,
      erro.codigo ?? 'ERRO',
      erro.mensagem ?? `O servidor respondeu com erro ${resposta.status}.`,
      erro.campos,
    )
  }
  return (resposta.status === 204 ? undefined : await resposta.json()) as T
}
