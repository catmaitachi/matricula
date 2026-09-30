import type { KeyboardEvent, ReactNode } from 'react'
import estilos from './Linhas.module.css'

export function Linhas({ rotulo, children }: { rotulo: string; children: ReactNode }) {
  return (
    <div role="table" aria-label={rotulo} className={estilos.tabela}>
      {children}
    </div>
  )
}

interface LinhaProps {
  indice: string
  children: ReactNode
  /** Sem `aoSelecionar`, a linha é só informação. */
  aoSelecionar?: () => void
  selecionada?: boolean
  /** Fica apagada e sem foco; o motivo deve aparecer em texto dentro da linha (nunca tachado). */
  indisponivel?: boolean
  /** Mais informação, que a linha abre no cursor, no foco ou quando selecionada. */
  detalhe?: ReactNode
}

/** Linha de lista: índice em mono; no cursor, no foco ou selecionada vira bloco invertido e abre o detalhe. */
export function Linha({ indice, children, aoSelecionar, selecionada, indisponivel, detalhe }: LinhaProps) {
  const interativa = aoSelecionar !== undefined && !indisponivel

  const aoTeclar = (e: KeyboardEvent<HTMLDivElement>) => {
    if (!interativa) return
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      aoSelecionar()
    } else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault()
      const linhas = [...(e.currentTarget.parentElement?.querySelectorAll<HTMLElement>('[role=row][tabindex="0"]') ?? [])]
      linhas[linhas.indexOf(e.currentTarget) + (e.key === 'ArrowDown' ? 1 : -1)]?.focus()
    }
  }

  return (
    <div
      role="row"
      className={`${estilos.linha} ${interativa ? estilos.interativa : ''}`}
      tabIndex={interativa ? 0 : undefined}
      aria-selected={aoSelecionar ? Boolean(selecionada) : undefined}
      aria-disabled={indisponivel || undefined}
      onClick={interativa ? aoSelecionar : undefined}
      onKeyDown={aoTeclar}
    >
      <span className={estilos.indice} role="cell" aria-hidden="true">
        {indice}
      </span>
      {children}
      {detalhe && (
        <div role="cell" className={estilos.detalhe}>
          <p>{detalhe}</p>
        </div>
      )}
    </div>
  )
}

/** Etiqueta em texto que explica o estado da linha (lotada, sem quórum...). */
export function Marca({ children }: { children: ReactNode }) {
  return <span className={estilos.marca}>{children}</span>
}

/** Painel que se abre logo abaixo de uma linha (formulário, opções): faixa com barra grossa à esquerda. É uma linha da tabela, para manter a estrutura ARIA válida. */
export function Painel({ children }: { children: ReactNode }) {
  return (
    <div role="row">
      <div role="cell" className={estilos.painel}>
        {children}
      </div>
    </div>
  )
}
