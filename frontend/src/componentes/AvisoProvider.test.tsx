import { act, fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AvisoProvider } from './AvisoProvider'
import { useAviso } from './contextoAviso'

function Disparador() {
  const { avisar } = useAviso()
  return (
    <>
      <button onClick={() => avisar('ok', 'Salvo com sucesso.')}>ok</button>
      <button onClick={() => avisar('erro', 'Limite atingido.')}>erro</button>
    </>
  )
}

const abrir = () =>
  render(
    <AvisoProvider>
      <Disparador />
    </AvisoProvider>,
  )

describe('AvisoProvider', () => {
  it('anuncia sucesso como status e erro como alerta', async () => {
    abrir()

    await userEvent.click(screen.getByRole('button', { name: 'ok' }))
    expect(screen.getByRole('status')).toHaveTextContent('Salvo com sucesso.')
    expect(screen.getByRole('alert')).toBeEmptyDOMElement()

    await userEvent.click(screen.getByRole('button', { name: 'erro' }))
    expect(screen.getByRole('alert')).toHaveTextContent('Limite atingido.')
    expect(screen.getByRole('status')).toBeEmptyDOMElement()
  })

  it('fecha pelo botão e pelo Esc', async () => {
    abrir()
    await userEvent.click(screen.getByRole('button', { name: 'ok' }))
    await userEvent.click(screen.getByRole('button', { name: 'Fechar aviso' }))
    expect(screen.getByRole('status')).toBeEmptyDOMElement()

    await userEvent.click(screen.getByRole('button', { name: 'erro' }))
    await userEvent.keyboard('{Escape}')
    expect(screen.getByRole('alert')).toBeEmptyDOMElement()
  })

  it('some sozinho depois de 6 segundos, e passar o mouse pausa a contagem', () => {
    vi.useFakeTimers()
    try {
      abrir()
      fireEvent.click(screen.getByRole('button', { name: 'ok' }))

      act(() => {
        vi.advanceTimersByTime(4000)
      })
      fireEvent.pointerEnter(screen.getByText('Salvo com sucesso.').closest('div')!)
      act(() => {
        vi.advanceTimersByTime(10_000)
      })
      expect(screen.getByRole('status')).toHaveTextContent('Salvo com sucesso.') // pausado

      fireEvent.pointerLeave(screen.getByText('Salvo com sucesso.').closest('div')!)
      act(() => {
        vi.advanceTimersByTime(2100)
      })
      expect(screen.getByRole('status')).toBeEmptyDOMElement()
    } finally {
      vi.useRealTimers()
    }
  })

  it('um aviso novo substitui o anterior e recomeça a contagem', () => {
    vi.useFakeTimers()
    try {
      abrir()
      fireEvent.click(screen.getByRole('button', { name: 'ok' }))
      act(() => {
        vi.advanceTimersByTime(5000)
      })
      fireEvent.click(screen.getByRole('button', { name: 'ok' }))
      act(() => {
        vi.advanceTimersByTime(5000)
      })
      expect(screen.getByRole('status')).toHaveTextContent('Salvo com sucesso.')
    } finally {
      vi.useRealTimers()
    }
  })
})
