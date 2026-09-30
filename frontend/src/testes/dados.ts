import type { Conta, CurriculoParaAluno, Curriculo, Disciplina, Matricula, MinhasMatriculas, TurmaDoProfessor } from '../api/tipos'

export const turmaAluno = (extra: Partial<CurriculoParaAluno['turmas'][number]> = {}): CurriculoParaAluno['turmas'][number] => ({
  id: 1, disciplina: 'Engenharia de Software', codigo: 'A', turno: 'MANHA', professor: 'Dr. Carlos', estado: 'ABERTA',
  ocupadas: 10, minAlunos: 3, maxAlunos: 60, minhaMatriculaId: null, ...extra,
})

export const curriculoAberto = (turmas = [turmaAluno()]): CurriculoParaAluno => ({ semestre: '2026/2', estado: 'ABERTO', turmas })

export const minhas = (extra: Partial<MinhasMatriculas> = {}): MinhasMatriculas => ({
  semestre: '2026/2', estado: 'ABERTO', obrigatorias: 0, limiteObrigatorias: 4, optativas: 0, limiteOptativas: 2, matriculas: [], ...extra,
})

export const matricula = (extra: Partial<Matricula> = {}): Matricula => ({
  id: 7, turmaId: 1, disciplina: 'Engenharia de Software', codigo: 'A', turno: 'MANHA', tipo: 'OBRIGATORIA',
  estado: 'ATIVA', cobranca: 'ENVIADA', ...extra,
})

export const conta = (extra: Partial<Conta> = {}): Conta => ({
  id: 3, numPessoa: 'ALU1', nome: 'Ana Souza', papel: 'ALUNO', numMatricula: '2026001', ativo: true, ...extra,
})

export const disciplina = (extra: Partial<Disciplina> = {}): Disciplina => ({
  id: 5, nome: 'Redes de Computadores', minAlunos: 3, maxAlunos: 60, ativa: true, cursos: [{ id: 1, nome: 'Engenharia de Software', numCreditos: 240 }], ...extra,
})

export const curriculoDetalhe = (extra: Partial<Curriculo> = {}): Curriculo => ({ id: 9, semestre: '2026/2', estado: 'RASCUNHO', turmas: [], ...extra })

export const turmaProfessor = (extra: Partial<TurmaDoProfessor> = {}): TurmaDoProfessor => ({
  id: 4, semestre: '2026/2', disciplina: 'Redes de Computadores', codigo: 'A', turno: 'NOITE', estado: 'ABERTA',
  matriculados: 2, minAlunos: 3, maxAlunos: 60, ...extra,
})
