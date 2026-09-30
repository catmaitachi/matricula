import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { curriculoAberto, matricula, minhas, turmaAluno } from '../../testes/dados'
import { renderizar } from '../../testes/render'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

async function abrir(turmas = [turmaAluno()], resumo = minhas()) {
  api.aluno.curriculo.mockResolvedValue(curriculoAberto(turmas))
  api.aluno.matriculas.mockResolvedValue(resumo)
  renderizar('/aluno/curriculo')
  await screen.findByRole('heading', { name: 'Currículo' })
}

const linha = (nome: RegExp) => screen.getByRole('row', { name: nome })

beforeEach(() => vi.resetAllMocks())

describe('currículo do aluno', () => {
  it('mostra o semestre, os limites e as turmas com as vagas', async () => {
    await abrir([turmaAluno({ ocupadas: 10 })], minhas({ obrigatorias: 2, optativas: 1 }))

    expect(screen.getByText('Semestre 2026/2')).toBeInTheDocument()
    expect(screen.getByText('Obrigatórias').parentElement).toHaveTextContent('2/4')
    expect(screen.getByText('Optativas').parentElement).toHaveTextContent('1/2')
    expect(linha(/Engenharia de Software/)).toHaveTextContent('Turma A · Manhã · Dr. Carlos')
    expect(linha(/Engenharia de Software/)).toHaveTextContent('10/60')
  })

  it('avisa por escrito quantos alunos faltam para a turma abrir', async () => {
    await abrir([turmaAluno({ ocupadas: 1, minAlunos: 3 })])
    expect(linha(/Engenharia/)).toHaveTextContent('faltam 2 para abrir')
  })

  it('matricula na turma escolhida, com o tipo escolhido, e avisa o resultado', async () => {
    await abrir()
    api.aluno.matricular.mockResolvedValue(matricula({ tipo: 'OPTATIVA' }))

    await userEvent.click(linha(/Engenharia/))
    expect(screen.getByRole('radio', { name: 'Obrigatória' })).toBeChecked()
    await userEvent.click(screen.getByRole('radio', { name: 'Optativa' }))
    await userEvent.click(screen.getByRole('button', { name: 'Matricular' }))

    expect(api.aluno.matricular).toHaveBeenCalledWith({ turmaId: 1, tipo: 'OPTATIVA' })
    expect(await screen.findByText(/Matriculado em Engenharia de Software\. Cobrança enviada\./)).toBeInTheDocument()
    expect(api.aluno.curriculo).toHaveBeenCalledTimes(2) // recarrega vagas e limites
  })

  it('mostra o motivo quando a regra recusa (limite, lotada, pagamento fora do ar)', async () => {
    await abrir()
    api.aluno.matricular.mockRejectedValue(new ErroApi(409, 'TURMA_LOTADA', 'A turma está lotada.'))

    await userEvent.click(linha(/Engenharia/))
    await userEvent.click(screen.getByRole('button', { name: 'Matricular' }))

    expect(await screen.findByRole('alert', { name: '' })).toBeInTheDocument()
    expect(await screen.findByText(/A turma está lotada\./)).toBeInTheDocument()
  })

  it('turma lotada fica apagada, sem foco, com "lotada" escrito, e não abre o painel', async () => {
    await abrir([turmaAluno({ ocupadas: 60, maxAlunos: 60 })])
    const l = linha(/Engenharia/)

    expect(l).toHaveTextContent('lotada')
    expect(l).toHaveAttribute('aria-disabled', 'true')
    await userEvent.click(l)
    expect(screen.queryByRole('button', { name: 'Matricular' })).not.toBeInTheDocument()
  })

  it('no limite do tipo, explica e não deixa matricular; trocar o tipo libera', async () => {
    await abrir([turmaAluno()], minhas({ obrigatorias: 4 }))

    await userEvent.click(linha(/Engenharia/))
    expect(screen.getByText(/já tem o máximo de obrigatórias/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Matricular' })).toBeDisabled()

    await userEvent.click(screen.getByRole('radio', { name: 'Optativa' }))
    expect(screen.getByRole('button', { name: 'Matricular' })).toBeEnabled()
  })

  it('turma em que já estou aponta para "Minhas matrículas" em vez de oferecer matrícula', async () => {
    await abrir([turmaAluno({ minhaMatriculaId: 7 })])

    expect(linha(/Engenharia/)).toHaveTextContent('matriculado')
    await userEvent.click(linha(/Engenharia/))
    expect(within(screen.getByRole('main')).getByRole('link', { name: 'Minhas matrículas' })).toHaveAttribute('href', '/aluno/matriculas')
    expect(screen.queryByRole('button', { name: 'Matricular' })).not.toBeInTheDocument()
  })

  it('sem inscrições abertas, explica em vez de mostrar erro', async () => {
    api.aluno.curriculo.mockRejectedValue(new ErroApi(404, 'SEM_CURRICULO_ABERTO', 'Não há inscrições abertas no momento.'))
    api.aluno.matriculas.mockResolvedValue(minhas({ semestre: null, estado: null }))
    renderizar('/aluno/curriculo')

    expect(await screen.findByText(/Não há inscrições abertas no momento/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Tentar de novo' })).not.toBeInTheDocument()
  })

  it('falha de rede vira mensagem com "tentar de novo"', async () => {
    api.aluno.curriculo.mockRejectedValue(new ErroApi(0, 'SEM_CONEXAO', 'Não foi possível falar com o servidor.'))
    api.aluno.matriculas.mockResolvedValue(minhas())
    renderizar('/aluno/curriculo')

    const alerta = await screen.findByText('Não foi possível falar com o servidor.')
    expect(within(alerta.closest('[role=alert]') as HTMLElement).getByRole('button', { name: 'Tentar de novo' })).toBeInTheDocument()
  })
})
