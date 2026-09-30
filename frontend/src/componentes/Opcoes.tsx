import type { ReactNode } from 'react'
import estilos from './Opcoes.module.css'

interface Opcao<V extends string> {
  valor: V
  rotulo: ReactNode
}

interface Props<V extends string> {
  legenda: string
  nome: string
  valor: V
  opcoes: readonly Opcao<V>[]
  aoMudar: (valor: V) => void
}

/** Escolha entre poucas alternativas: botões de rádio nativos vestidos de segmentos que enchem quando marcados. */
export function Opcoes<V extends string>({ legenda, nome, valor, opcoes, aoMudar }: Props<V>) {
  return (
    <fieldset className={estilos.grupo}>
      <legend className={estilos.legenda}>{legenda}</legend>
      <div className={estilos.opcoes}>
        {opcoes.map((o) => (
          <label key={o.valor} className={estilos.opcao}>
            <input type="radio" name={nome} value={o.valor} checked={valor === o.valor} onChange={() => aoMudar(o.valor)} />
            <span>{o.rotulo}</span>
          </label>
        ))}
      </div>
    </fieldset>
  )
}
