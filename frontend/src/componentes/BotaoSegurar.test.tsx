import { act, fireEvent, render, screen } from '@testing-library/react'
import { segurar } from '../testes/segurar'
import { BotaoSegurar } from './BotaoSegurar'

const abrir = (aoConfirmar = vi.fn(), duracao = 1000) => {
  render(
    <BotaoSegurar concluido="Feito" dica="Segure por 1 segundo." aoConfirmar={aoConfirmar} duracao={duracao}>
      Segure para cancelar
    </BotaoSegurar>,
  )
  return { botao: screen.getByRole('button'), aoConfirmar }
}

describe('BotaoSegurar', () => {
  it('descreve o gesto para quem usa leitor de tela', () => {
    const { botao } = abrir()
    expect(botao).toHaveAccessibleDescription('Segure por 1 segundo.')
  })

  it('confirma quando se segura pelo tempo todo', () => {
    const { botao, aoConfirmar } = abrir()
    segurar(botao, 1100)
    expect(aoConfirmar).toHaveBeenCalledOnce()
    expect(botao).toHaveAttribute('data-fase', 'feito')
  })

  it('não confirma se soltar antes do fim e o preenchimento volta', () => {
    const { botao, aoConfirmar } = abrir()
    segurar(botao, 600)
    expect(aoConfirmar).not.toHaveBeenCalled()
    expect(botao).toHaveAttribute('data-fase', 'ocioso')
  })

  it('a duração é configurável', () => {
    const { botao, aoConfirmar } = abrir(vi.fn(), 2000)
    segurar(botao, 1500)
    expect(aoConfirmar).not.toHaveBeenCalled()
    segurar(botao, 2100)
    expect(aoConfirmar).toHaveBeenCalledOnce()
  })

  it('funciona pelo teclado (Espaço) e ignora a repetição da tecla', () => {
    const { botao, aoConfirmar } = abrir()
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout', 'requestAnimationFrame', 'cancelAnimationFrame', 'performance'] })
    try {
      fireEvent.keyDown(botao, { key: ' ' })
      fireEvent.keyDown(botao, { key: ' ', repeat: true })
      act(() => {
        vi.advanceTimersByTime(1100)
      })
      fireEvent.keyUp(botao, { key: ' ' })
    } finally {
      vi.useRealTimers()
    }
    expect(aoConfirmar).toHaveBeenCalledOnce()
  })

  it('não confirma duas vezes enquanto mostra "feito"', () => {
    const { botao, aoConfirmar } = abrir()
    segurar(botao, 1100)
    segurar(botao, 1100)
    expect(aoConfirmar).toHaveBeenCalledOnce()
  })
})
