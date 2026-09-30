import { render, screen } from '@testing-library/react'
import { createRef } from 'react'
import { Campo } from './Campo'

describe('Campo', () => {
  it('associa o rótulo ao campo', () => {
    render(<Campo rotulo="Senha" />)
    expect(screen.getByLabelText('Senha')).toBeInTheDocument()
  })

  it('sem erro, o campo não é inválido e não há alerta', () => {
    render(<Campo rotulo="Senha" />)
    expect(screen.getByLabelText('Senha')).not.toHaveAttribute('aria-invalid')
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('marca o erro como inválido e o anuncia como alerta', () => {
    render(<Campo rotulo="Senha" erro="Informe sua senha." />)

    const campo = screen.getByLabelText('Senha')
    expect(campo).toBeInvalid()
    expect(campo).toHaveAccessibleDescription('Informe sua senha.')
    expect(screen.getByRole('alert')).toHaveTextContent('Informe sua senha.')
  })

  it('repassa o ref para o input', () => {
    const ref = createRef<HTMLInputElement>()
    render(<Campo rotulo="Nº de pessoa" ref={ref} />)
    expect(ref.current).toBe(screen.getByLabelText('Nº de pessoa'))
  })
})
