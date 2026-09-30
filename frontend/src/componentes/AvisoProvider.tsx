import { useEffect, useRef, useState, type ReactNode } from 'react'
import { X } from 'lucide-react'
import { Icone } from './Icone'
import { AvisoContexto, type TipoAviso } from './contextoAviso'
import estilos from './Aviso.module.css'

const DURACAO_MS = 6000

interface Aviso {
  id: number
  tipo: TipoAviso
  texto: string
}

/**
 * Toast que sobe pela borda de baixo e queima um pavio de 6 s. Passar o mouse ou focar pausa; Esc fecha.
 * Há duas regiões sempre montadas (uma educada, outra assertiva) para leitores de tela anunciarem de forma confiável.
 */
export function AvisoProvider({ children }: { children: ReactNode }) {
  const [aviso, setAviso] = useState<Aviso | null>(null)
  const [pausado, setPausado] = useState(false)
  const proximo = useRef(1)
  const restante = useRef(DURACAO_MS)
  const atual = useRef(0)

  const fechar = () => setAviso(null)

  useEffect(() => {
    if (!aviso || pausado) return
    const meu = aviso.id
    const inicio = Date.now()
    const temporizador = setTimeout(() => setAviso(null), restante.current)
    return () => {
      clearTimeout(temporizador)
      // só desconta o que este aviso gastou; se já chegou outro, o relógio é dele
      if (atual.current === meu) restante.current -= Date.now() - inicio
    }
  }, [aviso, pausado])

  useEffect(() => {
    if (!aviso) return
    const aoTeclar = (e: KeyboardEvent) => e.key === 'Escape' && setAviso(null)
    window.addEventListener('keydown', aoTeclar)
    return () => window.removeEventListener('keydown', aoTeclar)
  }, [aviso])

  const avisar = (tipo: TipoAviso, texto: string) => {
    restante.current = DURACAO_MS
    setPausado(false)
    atual.current = proximo.current++
    setAviso({ id: atual.current, tipo, texto })
  }

  const toast = (tipo: TipoAviso) =>
    aviso?.tipo === tipo && (
      <div
        key={aviso.id}
        className={`${estilos.aviso} ${tipo === 'erro' ? estilos.erro : estilos.ok}`}
        onPointerEnter={() => setPausado(true)}
        onPointerLeave={() => setPausado(false)}
        onFocus={() => setPausado(true)}
        onBlur={() => setPausado(false)}
      >
        <p>
          <b className={estilos.rotulo}>{tipo === 'erro' ? 'Não foi possível' : 'Feito'}</b>
          {aviso.texto}
        </p>
        <button type="button" className={estilos.fechar} onClick={fechar} aria-label="Fechar aviso">
          <Icone de={X} size={18} />
        </button>
        <i className={estilos.pavio} style={{ animationDuration: `${DURACAO_MS}ms`, animationPlayState: pausado ? 'paused' : 'running' }} />
      </div>
    )

  return (
    <AvisoContexto value={{ avisar }}>
      {children}
      <div className={estilos.regiao}>
        <div role="status">{toast('ok')}</div>
        <div role="alert">{toast('erro')}</div>
      </div>
    </AvisoContexto>
  )
}
