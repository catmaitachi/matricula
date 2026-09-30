import type { ReactNode } from 'react'
import { TriangleAlert } from 'lucide-react'
import { Botao } from './Botao'
import { Icone } from './Icone'
import type { EstadoCarga } from './useCarregar'
import estilos from './Carga.module.css'

interface Props<T> {
  carga: EstadoCarga<T>
  tentar: () => void
  children: (dados: T) => ReactNode
}

/** Mostra "carregando", o erro com "tentar de novo", ou o conteúdo quando os dados chegam. */
export function Carga<T>({ carga, tentar, children }: Props<T>) {
  if (carga.status === 'carregando') {
    return (
      <p role="status" className={estilos.carregando}>
        Carregando…
      </p>
    )
  }
  if (carga.status === 'erro') {
    return (
      <div role="alert" className={estilos.erro}>
        <p>
          <Icone de={TriangleAlert} size={18} /> {carga.erro.message}
        </p>
        <Botao onClick={tentar}>Tentar de novo</Botao>
      </div>
    )
  }
  return <>{children(carga.dados)}</>
}
