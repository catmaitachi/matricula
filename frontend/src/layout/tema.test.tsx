import { act, renderHook } from '@testing-library/react'
import { useTema } from './tema'

describe('useTema', () => {
  beforeEach(() => {
    localStorage.clear()
    delete document.documentElement.dataset.tema
  })

  it('começa no automático, sem marcar o documento', () => {
    const { result } = renderHook(() => useTema())
    expect(result.current.tema).toBe('auto')
    expect(document.documentElement.dataset.tema).toBeUndefined()
  })

  it('alternar percorre automático, claro, escuro e volta, marcando o documento', () => {
    const { result } = renderHook(() => useTema())

    act(() => result.current.alternar())
    expect(result.current.tema).toBe('claro')
    expect(document.documentElement.dataset.tema).toBe('claro')

    act(() => result.current.alternar())
    expect(result.current.tema).toBe('escuro')
    expect(document.documentElement.dataset.tema).toBe('escuro')

    act(() => result.current.alternar())
    expect(result.current.tema).toBe('auto')
    expect(document.documentElement.dataset.tema).toBeUndefined()
  })

  it('guarda a escolha só como preferência visual e lembra dela', () => {
    const { result } = renderHook(() => useTema())
    act(() => result.current.alternar())
    expect(localStorage.getItem('matricula.tema')).toBe('claro')

    const outra = renderHook(() => useTema())
    expect(outra.result.current.tema).toBe('claro')

    act(() => outra.result.current.alternar())
    act(() => outra.result.current.alternar())
    expect(localStorage.getItem('matricula.tema')).toBeNull()
  })

  it('valor guardado inválido é ignorado', () => {
    localStorage.setItem('matricula.tema', 'roxo')
    const { result } = renderHook(() => useTema())
    expect(result.current.tema).toBe('auto')
  })
})
