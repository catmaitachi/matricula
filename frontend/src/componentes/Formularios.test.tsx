import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Opcoes } from './Opcoes'
import { Cobranca, Caixinhas, Registro } from './Registro'
import { Regua } from './Regua'
import { Seletor } from './Seletor'

describe('Opcoes', () => {
  it('é um grupo de rádio nativo com legenda, e muda o valor', async () => {
    const aoMudar = vi.fn()
    render(
      <Opcoes
        legenda="Tipo"
        nome="tipo"
        valor="A"
        aoMudar={aoMudar}
        opcoes={[
          { valor: 'A', rotulo: 'Obrigatória' },
          { valor: 'B', rotulo: 'Optativa' },
        ]}
      />,
    )
    expect(screen.getByRole('group', { name: 'Tipo' })).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: 'Obrigatória' })).toBeChecked()

    await userEvent.click(screen.getByRole('radio', { name: 'Optativa' }))

    expect(aoMudar).toHaveBeenCalledWith('B')
  })
})

describe('Regua', () => {
  it('mostra o valor, as marcas de referência e avisa a mudança como número', () => {
    const aoMudar = vi.fn()
    render(<Regua rotulo="Máximo" valor={60} min={1} max={100} marcas={[{ valor: 60, rotulo: 'padrão 60' }]} aoMudar={aoMudar} />)

    expect(screen.getByRole('slider', { name: 'Máximo' })).toHaveValue('60')
    expect(screen.getByText('padrão 60')).toBeInTheDocument()

    fireEvent.change(screen.getByRole('slider'), { target: { value: '45' } })
    expect(aoMudar).toHaveBeenCalledWith(45)
  })
})

describe('Seletor', () => {
  it('associa o rótulo e anuncia o erro', () => {
    render(
      <Seletor rotulo="Turno" erro="Escolha um turno.">
        <option value="">Escolha…</option>
      </Seletor>,
    )
    const seletor = screen.getByLabelText('Turno')
    expect(seletor).toBeInvalid()
    expect(seletor).toHaveAccessibleDescription('Escolha um turno.')
    expect(screen.getByRole('alert')).toHaveTextContent('Escolha um turno.')
  })
})

describe('Registro', () => {
  it('lê valor e rótulo juntos; as caixinhas não são lidas e o limite atingido aparece escrito', () => {
    render(
      <Registro rotulo="Obrigatórias" alerta>
        4/4
        <Caixinhas usadas={4} total={4} />
      </Registro>,
    )
    expect(screen.getByText('Obrigatórias').parentElement).toHaveTextContent('4/4Obrigatóriaslimite atingido')
  })

  it('sem alerta, não escreve limite atingido', () => {
    render(<Registro rotulo="Optativas">1/2</Registro>)
    expect(screen.queryByText('limite atingido')).not.toBeInTheDocument()
  })

  it.each([
    ['PENDENTE', 'pendente'],
    ['ENVIADA', 'enviada'],
    ['CANCELAMENTO_PENDENTE', 'cancelando'],
    ['CANCELADA', 'cancelada'],
  ] as const)('a cobrança %s aparece em texto (não só por forma ou cor)', (status, texto) => {
    render(<Cobranca status={status} />)
    expect(screen.getByText(texto)).toBeInTheDocument()
  })
})
