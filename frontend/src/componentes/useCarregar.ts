import { useEffect, useState } from 'react'

export type EstadoCarga<T> = { status: 'carregando' } | { status: 'erro'; erro: Error } | { status: 'ok'; dados: T }

/**
 * Busca dados ao montar (e quando `dependencias` mudam). Ao recarregar, mantém os dados antigos na tela até
 * chegarem os novos, sem piscar. Devolve o estado e uma função para recarregar.
 */
export function useCarregar<T>(buscar: () => Promise<T>, dependencias: readonly unknown[] = []): [EstadoCarga<T>, () => void] {
  const [estado, setEstado] = useState<EstadoCarga<T>>({ status: 'carregando' })
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    buscar().then(
      (dados) => ativo && setEstado({ status: 'ok', dados }),
      (erro: unknown) => ativo && setEstado({ status: 'erro', erro: erro instanceof Error ? erro : new Error(String(erro)) }),
    )
    return () => {
      ativo = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- `buscar` é recriada a cada render; o que decide é `dependencias`
  }, [versao, ...dependencias])

  return [estado, () => setVersao((v) => v + 1)]
}
