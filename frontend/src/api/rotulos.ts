import type { EstadoCurriculo, EstadoTurma, Papel, StatusCobranca, TipoMatricula, Turno } from './tipos'

export const ROTULO_PAPEL: Record<Papel, string> = { ALUNO: 'Aluno', PROFESSOR: 'Professor', SECRETARIA: 'Secretaria' }
export const ROTULO_TIPO: Record<TipoMatricula, string> = { OBRIGATORIA: 'Obrigatória', OPTATIVA: 'Optativa' }
export const ROTULO_TURNO: Record<Turno, string> = { MANHA: 'Manhã', TARDE: 'Tarde', NOITE: 'Noite' }
export const ROTULO_ESTADO_TURMA: Record<EstadoTurma, string> = { ABERTA: 'aberta', CONFIRMADA: 'confirmada', CANCELADA: 'cancelada' }
export const ROTULO_ESTADO_CURRICULO: Record<EstadoCurriculo, string> = {
  RASCUNHO: 'rascunho',
  ABERTO: 'inscrições abertas',
  ENCERRADO: 'encerrado',
}
export const ROTULO_COBRANCA: Record<StatusCobranca, string> = {
  PENDENTE: 'pendente',
  ENVIADA: 'enviada',
  CANCELAMENTO_PENDENTE: 'cancelando',
  CANCELADA: 'cancelada',
}
