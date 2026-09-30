import { requisitar } from './http'
import type {
  AlunoNaTurma, Conta, Curriculo, CurriculoParaAluno, CurriculoResumo, Curso, DadosCurso, DadosDisciplina, Disciplina,
  EdicaoConta, Encerramento, Matricula, MinhasMatriculas, NovaConta, NovaMatricula, NovaTurma, Papel, Turma,
  TurmaDoProfessor, Usuario,
} from './tipos'

const ler = <T>(caminho: string) => requisitar<T>(caminho)
const enviar = <T>(caminho: string, json?: unknown) => requisitar<T>(caminho, { metodo: 'POST', json })
const trocar = <T>(caminho: string, json: unknown) => requisitar<T>(caminho, { metodo: 'PUT', json })
const apagar = (caminho: string) => requisitar<void>(caminho, { metodo: 'DELETE' })

/** Único ponto de contato com o backend. Os caminhos espelham os controllers de `backend/api`. */
export const api = {
  entrar: (numPessoa: string, senha: string) =>
    requisitar<void>('/auth/entrar', { metodo: 'POST', formulario: { numPessoa, senha } }),
  sair: () => requisitar<void>('/auth/sair', { metodo: 'POST' }),
  eu: () => ler<Usuario>('/auth/eu'),

  contas: {
    listar: (papel: Papel) => ler<Conta[]>(`/contas?papel=${papel}`),
    criar: (conta: NovaConta) => enviar<Conta>('/contas', conta),
    atualizar: (id: number, conta: EdicaoConta) => trocar<Conta>(`/contas/${id}`, conta),
  },
  cursos: {
    listar: () => ler<Curso[]>('/cursos'),
    criar: (curso: DadosCurso) => enviar<Curso>('/cursos', curso),
  },
  disciplinas: {
    listar: () => ler<Disciplina[]>('/disciplinas'),
    criar: (disciplina: DadosDisciplina) => enviar<Disciplina>('/disciplinas', disciplina),
    atualizar: (id: number, disciplina: DadosDisciplina) => trocar<Disciplina>(`/disciplinas/${id}`, disciplina),
  },
  curriculos: {
    listar: () => ler<CurriculoResumo[]>('/curriculos'),
    criar: (semestre: string) => enviar<CurriculoResumo>('/curriculos', { semestre }),
    detalhe: (id: number) => ler<Curriculo>(`/curriculos/${id}`),
    adicionarTurma: (id: number, turma: NovaTurma) => enviar<Turma>(`/curriculos/${id}/turmas`, turma),
    removerTurma: (id: number, turmaId: number) => apagar(`/curriculos/${id}/turmas/${turmaId}`),
    abrir: (id: number) => enviar<CurriculoResumo>(`/curriculos/${id}/abrir`),
    encerrar: (id: number) => enviar<Encerramento>(`/curriculos/${id}/encerrar`),
  },
  aluno: {
    curriculo: () => ler<CurriculoParaAluno>('/aluno/curriculo'),
    matriculas: () => ler<MinhasMatriculas>('/aluno/matriculas'),
    matricular: (pedido: NovaMatricula) => enviar<Matricula>('/aluno/matriculas', pedido),
    cancelar: (id: number) => apagar(`/aluno/matriculas/${id}`),
  },
  professor: {
    turmas: () => ler<TurmaDoProfessor[]>('/professor/turmas'),
    alunos: (turmaId: number) => ler<AlunoNaTurma[]>(`/professor/turmas/${turmaId}/alunos`),
  },
}
