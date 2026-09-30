import { useEffect, useId, useRef, useState, type ReactNode } from 'react'
import estilos from './BotaoSegurar.module.css'

interface Props {
  children: ReactNode
  concluido: string
  dica: string
  aoConfirmar: () => void | Promise<void>
  duracao?: number
}

/**
 * Ação sem volta pede pressão longa: o bloco vermelho corre da esquerda para a direita enquanto se segura (mouse, toque ou Espaço/Enter)
 * e recua se soltar antes do fim, com a contagem regressiva escrita. Substitui a janela de "tem certeza?".
 */
export function BotaoSegurar({ children, concluido, dica, aoConfirmar, duracao = 1000 }: Props) {
  const botao = useRef<HTMLButtonElement>(null)
  const anim = useRef({ quadro: 0, inicio: 0, progresso: 0, segurando: false })
  const [feito, setFeito] = useState(false)
  const idDica = useId()

  const pintar = (p: number) => {
    anim.current.progresso = p
    botao.current?.style.setProperty('--p', String(p))
    // contagem regressiva escrita (só para os olhos; a descrição do botão já diz o gesto)
    const texto = `${(duracao / 1000 * (1 - p)).toFixed(1).replace('.', ',')} s`
    botao.current?.querySelectorAll('[data-contagem]').forEach((el) => (el.textContent = texto))
  }

  useEffect(() => () => cancelAnimationFrame(anim.current.quadro), [])

  const passo = (agora: number) => {
    const a = anim.current
    if (!a.segurando) return
    const p = Math.min(1, (agora - a.inicio) / duracao)
    pintar(p)
    if (p < 1) {
      a.quadro = requestAnimationFrame(passo)
      return
    }
    a.segurando = false
    setFeito(true)
    void aoConfirmar()
    setTimeout(() => {
      setFeito(false)
      pintar(0)
    }, 700)
  }

  const comecar = () => {
    const a = anim.current
    if (a.segurando || feito) return
    a.segurando = true
    a.inicio = performance.now() - a.progresso * duracao
    a.quadro = requestAnimationFrame(passo)
  }

  const soltar = () => {
    const a = anim.current
    if (!a.segurando) return
    a.segurando = false
    cancelAnimationFrame(a.quadro)
    const de = a.progresso
    const t0 = performance.now()
    const voltar = (agora: number) => {
      const k = Math.min(1, (agora - t0) / 200)
      pintar(de * (1 - k))
      if (k < 1 && !anim.current.segurando) requestAnimationFrame(voltar)
    }
    requestAnimationFrame(voltar)
  }

  return (
    <span className={estilos.grupo}>
      <button
        ref={botao}
        type="button"
        className={estilos.botao}
        data-fase={feito ? 'feito' : 'ocioso'}
        aria-describedby={idDica}
        onPointerDown={(e) => {
          e.currentTarget.setPointerCapture?.(e.pointerId)
          comecar()
        }}
        onPointerUp={soltar}
        onPointerCancel={soltar}
        onPointerLeave={soltar}
        onBlur={soltar}
        onKeyDown={(e) => {
          if ((e.key === ' ' || e.key === 'Enter') && !e.repeat) {
            e.preventDefault()
            comecar()
          }
        }}
        onKeyUp={(e) => (e.key === ' ' || e.key === 'Enter') && soltar()}
      >
        <span className={estilos.camada}>
          <span className={estilos.inicial}>{children}</span>
          <span className={estilos.final}>{concluido}</span>
          <span className={estilos.contagem} data-contagem aria-hidden="true">
            {(duracao / 1000).toFixed(1).replace('.', ',')} s
          </span>
        </span>
        <span className={`${estilos.camada} ${estilos.cheia}`} aria-hidden="true">
          <span className={estilos.inicial}>{children}</span>
          <span className={estilos.final}>{concluido}</span>
          <span className={estilos.contagem} data-contagem>
            {(duracao / 1000).toFixed(1).replace('.', ',')} s
          </span>
        </span>
      </button>
      <span id={idDica} className={estilos.dica}>
        {dica}
      </span>
    </span>
  )
}
