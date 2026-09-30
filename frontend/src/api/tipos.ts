export type Papel = 'ALUNO' | 'PROFESSOR' | 'SECRETARIA'
export type TipoMatricula = 'OBRIGATORIA' | 'OPTATIVA'
export type Turno = 'MANHA' | 'TARDE' | 'NOITE'
export type EstadoTurma = 'ABERTA' | 'CONFIRMADA' | 'CANCELADA'
export type EstadoCurriculo = 'RASCUNHO' | 'ABERTO' | 'ENCERRADO'
export type EstadoMatricula = 'ATIVA' | 'CANCELADA_SEM_QUORUM'
export type StatusCobranca = 'PENDENTE' | 'ENVIADA' | 'CANCELAMENTO_PENDENTE' | 'CANCELADA'

export interface Usuario {
  nome: string
  numPessoa: string
  papel: Papel
}

export interface Conta {
  id: number
  numPessoa: string
  nome: string
  papel: Papel
  numMatricula: string | null
  ativo: boolean
}

export interface Curso {
  id: number
  nome: string
  numCreditos: number
}

export interface Disciplina {
  id: number
  nome: string
  minAlunos: number
  maxAlunos: number
  ativa: boolean
  cursos: Curso[]
}

export interface Turma {
  id: number
  disciplinaId: number
  disciplina: string
  codigo: string
  turno: Turno
  professorId: number
  professor: string
  estado: EstadoTurma
  ocupadas: number
  minAlunos: number
  maxAlunos: number
}

export interface CurriculoResumo {
  id: number
  semestre: string
  estado: EstadoCurriculo
  turmas: number
}

export interface Curriculo {
  id: number
  semestre: string
  estado: EstadoCurriculo
  turmas: Turma[]
}

export interface Encerramento {
  turmasCanceladas: number
  matriculasCanceladas: number
}

export interface TurmaParaAluno {
  id: number
  disciplina: string
  codigo: string
  turno: Turno
  professor: string
  estado: EstadoTurma
  ocupadas: number
  minAlunos: number
  maxAlunos: number
  minhaMatriculaId: number | null
}

export interface CurriculoParaAluno {
  semestre: string
  estado: EstadoCurriculo
  turmas: TurmaParaAluno[]
}

export interface Matricula {
  id: number
  turmaId: number
  disciplina: string
  codigo: string
  turno: Turno
  tipo: TipoMatricula
  estado: EstadoMatricula
  cobranca: StatusCobranca
}

export interface MinhasMatriculas {
  semestre: string | null
  estado: EstadoCurriculo | null
  obrigatorias: number
  limiteObrigatorias: number
  optativas: number
  limiteOptativas: number
  matriculas: Matricula[]
}

export interface TurmaDoProfessor {
  id: number
  semestre: string
  disciplina: string
  codigo: string
  turno: Turno
  estado: EstadoTurma
  matriculados: number
  minAlunos: number
  maxAlunos: number
}

export interface AlunoNaTurma {
  nome: string
  numPessoa: string
  numMatricula: string | null
  tipo: TipoMatricula
}

export interface NovaConta {
  papel: Papel
  numPessoa: string
  nome: string
  senha: string
  numMatricula?: string
}

export interface EdicaoConta {
  nome: string
  ativo: boolean
  numMatricula?: string
  novaSenha?: string
}

export interface DadosCurso {
  nome: string
  numCreditos: number
}

export interface DadosDisciplina {
  nome: string
  minAlunos: number
  maxAlunos: number
  ativa: boolean
  cursoIds: number[]
}

export interface NovaTurma {
  disciplinaId: number
  professorId: number
  turno: Turno
}

export interface NovaMatricula {
  turmaId: number
  tipo: TipoMatricula
}

/** Erro devolvido pela API (ou falha de rede, com status 0). `message` já vem pronta para o usuário. */
export class ErroApi extends Error {
  readonly status: number
  readonly codigo: string
  /** Mensagens por campo, quando a API recusa a validação de um formulário. */
  readonly campos: Record<string, string>

  constructor(status: number, codigo: string, mensagem: string, campos: Record<string, string> = {}) {
    super(mensagem)
    this.name = 'ErroApi'
    this.status = status
    this.codigo = codigo
    this.campos = campos
  }
}
