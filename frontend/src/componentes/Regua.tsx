import { useId } from 'react'
import estilos from './Regua.module.css'

interface Marca {
  valor: number
  rotulo: string
}

interface Props {
  rotulo: string
  valor: number
  min: number
  max: number
  marcas?: readonly Marca[]
  aoMudar: (valor: number) => void
}

/** Escala contínua: dá para escolher qualquer ponto, e as referências que o sistema conhece (o padrão, o limite) ficam marcadas. */
export function Regua({ rotulo, valor, min, max, marcas = [], aoMudar }: Props) {
  const id = useId()
  const fracao = (v: number) => (v - min) / (max - min)

  return (
    <div className={estilos.regua} style={{ '--v': fracao(valor) } as React.CSSProperties}>
      <div className={estilos.topo}>
        <label htmlFor={id} className={estilos.rotulo}>
          {rotulo}
        </label>
        <output htmlFor={id} className={estilos.valor}>
          {valor}
        </output>
      </div>
      <input id={id} type="range" min={min} max={max} value={valor} onChange={(e) => aoMudar(Number(e.target.value))} />
      <div className={estilos.marcas} aria-hidden="true">
        {marcas.map((m) => (
          <span key={m.valor} style={{ left: `calc(${fracao(m.valor)} * (100% - 4px) + 2px)`, '--f': fracao(m.valor) } as React.CSSProperties}>
            {m.rotulo}
          </span>
        ))}
      </div>
    </div>
  )
}
