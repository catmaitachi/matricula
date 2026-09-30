import type { ButtonHTMLAttributes } from 'react'
import estilos from './Botao.module.css'

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  carregando?: boolean
}

/** Botão de contorno que enche de baixo para cima no hover. Enquanto carrega, fica desabilitado. */
export function Botao({ carregando, disabled, type = 'button', children, ...resto }: Props) {
  return (
    <button
      {...resto}
      type={type}
      className={estilos.botao}
      disabled={disabled || carregando}
      aria-busy={carregando || undefined}
    >
      {children}
    </button>
  )
}
