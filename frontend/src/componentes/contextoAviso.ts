import { createContext, useContext } from 'react'

export type TipoAviso = 'ok' | 'erro'

export interface Avisos {
  avisar(tipo: TipoAviso, texto: string): void
}

export const AvisoContexto = createContext<Avisos | null>(null)

export function useAviso(): Avisos {
  const avisos = useContext(AvisoContexto)
  if (!avisos) throw new Error('useAviso precisa estar dentro do AvisoProvider')
  return avisos
}
