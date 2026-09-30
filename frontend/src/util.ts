import { ErroApi } from './api/tipos'

export const pad2 = (n: number) => String(n).padStart(2, '0')

/** Texto seguro para mostrar ao usuário: a mensagem da API quando é dela, uma frase neutra nos demais casos. */
export const mensagemDe = (erro: unknown) => (erro instanceof ErroApi ? erro.message : 'Algo deu errado. Tente de novo.')

/** Erros por campo devolvidos pela API numa validação de formulário. */
export const camposDe = (erro: unknown): Record<string, string> => (erro instanceof ErroApi ? erro.campos : {})
