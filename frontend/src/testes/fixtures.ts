import type { Usuario } from '../api/tipos'

export const SESSAO_ALUNO: { usuario: Usuario } = {
  usuario: { nome: 'João Pedro', numPessoa: 'ALU999', papel: 'ALUNO' },
}

export const SESSAO_SECRETARIA: { usuario: Usuario } = {
  usuario: { nome: 'Maria Silva', numPessoa: 'SEC001', papel: 'SECRETARIA' },
}

export const SESSAO_PROFESSOR: { usuario: Usuario } = {
  usuario: { nome: 'Dr. Carlos Eduardo', numPessoa: 'PROF100', papel: 'PROFESSOR' },
}
