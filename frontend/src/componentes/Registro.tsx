import type { CSSProperties, ReactNode } from 'react'
import { ROTULO_COBRANCA } from '../api/rotulos'
import type { StatusCobranca } from '../api/tipos'
import estilos from './Registro.module.css'

interface RegistroProps {
  rotulo: string
  children: ReactNode
  /** Limite atingido: aparece escrito ao lado do rótulo (o estado nunca depende só de cor). */
  alerta?: boolean
}

/** Numeral grande com o rótulo ao lado, para números que o usuário precisa ler de relance. O limite fica escrito. */
export function Registro({ rotulo, children, alerta }: RegistroProps) {
  return (
    <div className={`${estilos.registro} ${alerta ? estilos.limitado : ''}`}>
      <span className={estilos.valor}>{children}</span>
      <span className={estilos.rotulo}>{rotulo}</span>
      {alerta && <span className={estilos.limite}>limite atingido</span>}
    </div>
  )
}

/** Caixinhas de contagem (3 de 4). O texto ao lado diz o número; as caixinhas só ajudam a ver. */
export function Caixinhas({ usadas, total }: { usadas: number; total: number }) {
  return (
    <span className={estilos.caixinhas} aria-hidden="true">
      {Array.from({ length: total }, (_, i) => (
        <i key={i} className={i < usadas ? estilos.cheia : undefined} />
      ))}
    </span>
  )
}

const ETAPA: Record<StatusCobranca, number> = { PENDENTE: 0.25, ENVIADA: 0.5, CANCELAMENTO_PENDENTE: 0.75, CANCELADA: 1 }

/** Situação da cobrança: a palavra e um traço cujo comprimento acompanha a etapa. */
export function Cobranca({ status }: { status: StatusCobranca }) {
  return (
    <span className={estilos.cobranca} data-status={status}>
      {ROTULO_COBRANCA[status]}
      <i className={estilos.traco} style={{ '--etapa': ETAPA[status] } as CSSProperties} aria-hidden="true" />
    </span>
  )
}
