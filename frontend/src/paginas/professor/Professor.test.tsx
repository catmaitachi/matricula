import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { turmaProfessor } from '../../testes/dados'
import { SESSAO_PROFESSOR } from '../../testes/fixtures'
import { renderizar } from '../../testes/render'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

beforeEach(() => vi.resetAllMocks())

describe('professor', () => {
  it('lista as turmas e abre a lista de chamada da escolhida', async () => {
    api.professor.turmas.mockResolvedValue([turmaProfessor(), turmaProfessor({ id: 5, disciplina: 'Banco de Dados', codigo: 'B' })])
    api.professor.alunos.mockResolvedValue([
      { nome: 'Ana Souza', numPessoa: 'ALU1', numMatricula: '2026001', tipo: 'OBRIGATORIA' },
      { nome: 'Bruno Lima', numPessoa: 'ALU2', numMatricula: '2026002', tipo: 'OPTATIVA' },
    ])
    renderizar('/professor/turmas', SESSAO_PROFESSOR.usuario)

    const linha = await screen.findByRole('row', { name: /Redes de Computadores/ })
    expect(linha).toHaveTextContent('2/60 alunos')
    await userEvent.click(linha)

    expect(await screen.findByRole('heading', { name: 'Redes de Computadores' })).toBeInTheDocument()
    expect(api.professor.alunos).toHaveBeenCalledWith(4)
    expect(screen.getByRole('row', { name: /Ana Souza/ })).toHaveTextContent('Matrícula 2026001 · ALU1')
    expect(screen.getByRole('row', { name: /Bruno Lima/ })).toHaveTextContent('Optativa')
    expect(screen.getByText('Mínimo para abrir').parentElement).toHaveTextContent('3')
  })

  it('sem turmas, explica quando elas aparecem', async () => {
    api.professor.turmas.mockResolvedValue([])
    renderizar('/professor/turmas', SESSAO_PROFESSOR.usuario)
    expect(await screen.findByText(/ainda não tem turmas em semestres abertos/)).toBeInTheDocument()
  })

  it('turma sem alunos diz isso', async () => {
    api.professor.turmas.mockResolvedValue([turmaProfessor({ matriculados: 0 })])
    api.professor.alunos.mockResolvedValue([])
    renderizar('/professor/turmas/4', SESSAO_PROFESSOR.usuario)
    expect(await screen.findByText('Nenhum aluno matriculado nesta turma ainda.')).toBeInTheDocument()
  })

  it('turma de outro professor responde como inexistente, com a mensagem da API', async () => {
    api.professor.turmas.mockResolvedValue([turmaProfessor()])
    api.professor.alunos.mockRejectedValue(new ErroApi(404, 'NAO_ENCONTRADO', 'Turma não encontrada.'))
    renderizar('/professor/turmas/99', SESSAO_PROFESSOR.usuario)
    expect(await screen.findByText('Turma não encontrada.')).toBeInTheDocument()
  })
})
