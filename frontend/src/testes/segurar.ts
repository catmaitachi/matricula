import { act, fireEvent } from '@testing-library/react'

/** Segura o botão por `ms` (com relógio falso) e solta. Usa o mesmo caminho do mouse/toque. */
export function segurar(botao: HTMLElement, ms: number) {
  vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'requestAnimationFrame', 'cancelAnimationFrame', 'performance'] })
  try {
    fireEvent.pointerDown(botao)
    act(() => {
      vi.advanceTimersByTime(ms)
    })
    fireEvent.pointerUp(botao)
  } finally {
    vi.useRealTimers()
  }
}
