import { screen } from '@testing-library/react'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { matricula, minhas } from '../../testes/dados'
import { renderizar } from '../../testes/render'
import { segurar } from '../../testes/segurar'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

async function abrir(resumo = minhas({ obrigatorias: 1, matriculas: [matricula()] })) {
  api.aluno.matriculas.mockResolvedValue(resumo)
  renderizar('/aluno/matriculas')
  await screen.findByRole('heading', { name: 'Minhas matrículas' })
}

beforeEach(() => vi.resetAllMocks())

describe('minhas matrículas', () => {
  it('lista a matrícula com tipo, turma e a situação da cobrança em texto', async () => {
    await abrir()

    const linha = screen.getByRole('row', { name: /Engenharia de Software/ })
    expect(linha).toHaveTextContent('Turma A · Manhã · Obrigatória')
    expect(linha).toHaveTextContent('enviada')
    expect(screen.getByText('Obrigatórias').parentElement).toHaveTextContent('1/4')
  })

  it('cancela segurando o botão, e só então chama a API', async () => {
    await abrir()
    api.aluno.cancelar.mockResolvedValue(undefined)
    const botao = screen.getByRole('button', { name: /Segure para cancelar/ })

    segurar(botao, 400)
    expect(api.aluno.cancelar).not.toHaveBeenCalled()

    segurar(botao, 1100)
    expect(api.aluno.cancelar).toHaveBeenCalledWith(7)
    expect(await screen.findByText(/Matrícula em Engenharia de Software cancelada e cobrança cancelada\./)).toBeInTheDocument()
    expect(api.aluno.matriculas).toHaveBeenCalledTimes(2)
  })

  it('mostra o motivo se o cancelamento falhar (ex.: pagamento fora do ar)', async () => {
    await abrir()
    api.aluno.cancelar.mockRejectedValue(new ErroApi(503, 'PAGAMENTO_INDISPONIVEL', 'O sistema de pagamento está indisponível.'))

    segurar(screen.getByRole('button', { name: /Segure para cancelar/ }), 1100)

    expect(await screen.findByText(/O sistema de pagamento está indisponível\./)).toBeInTheDocument()
  })

  it('turma cancelada por falta de quórum aparece apagada, explicada em texto e sem botão de cancelar', async () => {
    await abrir(
      minhas({ matriculas: [matricula({ estado: 'CANCELADA_SEM_QUORUM', cobranca: 'CANCELADA' })], estado: 'ENCERRADO' }),
    )

    const linha = screen.getByRole('row', { name: /Engenharia de Software/ })
    expect(linha).toHaveTextContent('cancelada: turma sem quórum')
    expect(linha).toHaveAttribute('aria-disabled', 'true')
    expect(screen.queryByRole('button', { name: /Segure para cancelar/ })).not.toBeInTheDocument()
  })

  it('depois de encerradas as inscrições não oferece cancelar', async () => {
    await abrir(minhas({ estado: 'ENCERRADO', obrigatorias: 1, matriculas: [matricula()] }))
    expect(screen.queryByRole('button', { name: /Segure para cancelar/ })).not.toBeInTheDocument()
  })

  it('sem matrículas, convida a ver o currículo', async () => {
    await abrir(minhas())
    expect(screen.getByRole('link', { name: 'Ver o currículo' })).toHaveAttribute('href', '/aluno/curriculo')
  })
})
