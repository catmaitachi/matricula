import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { apiMock as api } from './testes/apiMock'
import { semViolacoes } from './testes/axe'
import { conta, curriculoAberto, curriculoDetalhe, disciplina, matricula, minhas, turmaAluno, turmaProfessor } from './testes/dados'
import { SESSAO_ALUNO, SESSAO_PROFESSOR, SESSAO_SECRETARIA } from './testes/fixtures'
import { renderizar } from './testes/render'

vi.mock('./api', async () => (await import('./testes/apiMock')).moduloApi)

const TURMA_SEC = {
  id: 20, disciplinaId: 5, disciplina: 'Redes', codigo: 'A', turno: 'NOITE' as const, professorId: 8, professor: 'Dr. Carlos',
  estado: 'ABERTA' as const, ocupadas: 2, minAlunos: 3, maxAlunos: 60,
}

beforeEach(() => {
  vi.resetAllMocks()
  api.aluno.curriculo.mockResolvedValue(curriculoAberto([turmaAluno(), turmaAluno({ id: 2, disciplina: 'Cálculo II', ocupadas: 60 })]))
  api.aluno.matriculas.mockResolvedValue(minhas({ obrigatorias: 1, matriculas: [matricula(), matricula({ id: 8, estado: 'CANCELADA_SEM_QUORUM', cobranca: 'CANCELADA' })] }))
  api.professor.turmas.mockResolvedValue([turmaProfessor()])
  api.professor.alunos.mockResolvedValue([{ nome: 'Ana Souza', numPessoa: 'ALU1', numMatricula: '2026001', tipo: 'OBRIGATORIA' }])
  api.contas.listar.mockResolvedValue([conta(), conta({ id: 4, nome: 'Bia Lima', ativo: false })])
  api.cursos.listar.mockResolvedValue([{ id: 1, nome: 'Engenharia de Software', numCreditos: 240 }])
  api.disciplinas.listar.mockResolvedValue([disciplina()])
  api.curriculos.listar.mockResolvedValue([{ id: 9, semestre: '2026/2', estado: 'ABERTO', turmas: 2 }])
})

describe('acessibilidade das telas (axe)', () => {
  it('entrada', async () => {
    const { container } = renderizar('/entrar', null)
    await screen.findByRole('form', { name: 'Entrar' })
    await semViolacoes(container)
  })

  it('entrada com erros de validação visíveis', async () => {
    const { container } = renderizar('/entrar', null)
    await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }))
    await semViolacoes(container)
  })

  it('currículo do aluno, com o painel de matrícula aberto', async () => {
    const { container } = renderizar('/aluno/curriculo', SESSAO_ALUNO.usuario)
    await userEvent.click(await screen.findByRole('row', { name: /Engenharia de Software/ }))
    await screen.findByRole('button', { name: 'Matricular' })
    await semViolacoes(container)
  })

  it('minhas matrículas', async () => {
    const { container } = renderizar('/aluno/matriculas', SESSAO_ALUNO.usuario)
    await screen.findByRole('heading', { name: 'Minhas matrículas' })
    await semViolacoes(container)
  })

  it('turmas e lista de chamada do professor', async () => {
    const { container, unmount } = renderizar('/professor/turmas', SESSAO_PROFESSOR.usuario)
    await screen.findByRole('row', { name: /Redes/ })
    await semViolacoes(container)
    unmount()

    const outra = renderizar('/professor/turmas/4', SESSAO_PROFESSOR.usuario)
    await screen.findByRole('row', { name: /Ana Souza/ })
    await semViolacoes(outra.container)
  })

  it('contas, com o formulário de nova conta e o de edição', async () => {
    const { container } = renderizar('/secretaria/contas', SESSAO_SECRETARIA.usuario)
    await userEvent.click(await screen.findByRole('button', { name: 'Nova conta' }))
    await userEvent.click(await screen.findByRole('row', { name: /Ana Souza/ }))
    await screen.findByRole('form', { name: 'Editar conta' })
    await semViolacoes(container)
  })

  it('disciplinas, com o formulário aberto (régua, cursos, opções)', async () => {
    const { container } = renderizar('/secretaria/disciplinas', SESSAO_SECRETARIA.usuario)
    await userEvent.click(await screen.findByRole('button', { name: 'Nova disciplina' }))
    await userEvent.click(await screen.findByRole('button', { name: 'Novo curso' }))
    await semViolacoes(container)
  })

  it('currículos e currículo em detalhe (rascunho e aberto)', async () => {
    api.curriculos.detalhe.mockResolvedValue(curriculoDetalhe({ turmas: [TURMA_SEC] }))
    api.contas.listar.mockResolvedValue([conta({ id: 8, nome: 'Dr. Carlos', papel: 'PROFESSOR', numMatricula: null })])
    const { container, unmount } = renderizar('/secretaria/curriculos', SESSAO_SECRETARIA.usuario)
    await screen.findByRole('row', { name: /2026\/2/ })
    await semViolacoes(container)
    unmount()

    const rascunho = renderizar('/secretaria/curriculos/9', SESSAO_SECRETARIA.usuario)
    await screen.findByRole('form', { name: 'Adicionar turma' })
    await semViolacoes(rascunho.container)
    rascunho.unmount()

    api.curriculos.detalhe.mockResolvedValue(curriculoDetalhe({ estado: 'ABERTO', turmas: [TURMA_SEC] }))
    const aberto = renderizar('/secretaria/curriculos/9', SESSAO_SECRETARIA.usuario)
    await screen.findByRole('button', { name: /Segure para encerrar/ })
    await semViolacoes(aberto.container)
  })
})
