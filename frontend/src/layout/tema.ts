import { useState } from 'react'

export type Tema = 'auto' | 'claro' | 'escuro'

const CHAVE = 'matricula.tema'
const ORDEM: readonly Tema[] = ['auto', 'claro', 'escuro']

function lido(): Tema {
  try {
    const t = localStorage.getItem(CHAVE)
    return t === 'claro' || t === 'escuro' ? t : 'auto'
  } catch {
    return 'auto'
  }
}

/** Preferência de tema: automático (o do sistema), claro ou escuro. Fica só no navegador de quem escolheu. */
export function useTema() {
  const [tema, setTema] = useState<Tema>(lido)

  function alternar() {
    const proximo = ORDEM[(ORDEM.indexOf(tema) + 1) % ORDEM.length]!
    if (proximo === 'auto') delete document.documentElement.dataset.tema
    else document.documentElement.dataset.tema = proximo
    try {
      if (proximo === 'auto') localStorage.removeItem(CHAVE)
      else localStorage.setItem(CHAVE, proximo)
    } catch {
      /* sem armazenamento: vale só nesta visita */
    }
    setTema(proximo)
  }

  return { tema, alternar }
}
