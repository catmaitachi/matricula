import { vi } from 'vitest'

/** Substituto do módulo `api` para os testes das telas. Cada teste diz o que cada chamada devolve. */
export const apiMock = {
  entrar: vi.fn(),
  sair: vi.fn(),
  eu: vi.fn(),
  contas: { listar: vi.fn(), criar: vi.fn(), atualizar: vi.fn() },
  cursos: { listar: vi.fn(), criar: vi.fn() },
  disciplinas: { listar: vi.fn(), criar: vi.fn(), atualizar: vi.fn() },
  curriculos: {
    listar: vi.fn(),
    criar: vi.fn(),
    detalhe: vi.fn(),
    adicionarTurma: vi.fn(),
    removerTurma: vi.fn(),
    abrir: vi.fn(),
    encerrar: vi.fn(),
  },
  aluno: { curriculo: vi.fn(), matriculas: vi.fn(), matricular: vi.fn(), cancelar: vi.fn() },
  professor: { turmas: vi.fn(), alunos: vi.fn() },
}

export const moduloApi = { api: apiMock }
