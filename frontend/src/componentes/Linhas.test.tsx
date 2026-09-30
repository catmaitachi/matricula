import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Linha, Linhas } from './Linhas'

describe('Linhas', () => {
  it('o detalhe fica na linha como célula (texto lido pelo leitor de tela, mesmo recolhido)', () => {
    render(
      <Linhas rotulo="Turmas">
        <Linha indice="01" detalhe="60 vagas livres · mínimo de 3 para abrir">
          <strong role="cell">Redes</strong>
        </Linha>
      </Linhas>,
    )
    const linha = screen.getByRole('row', { name: /Redes/ })
    expect(within(linha).getByRole('cell', { name: '60 vagas livres · mínimo de 3 para abrir' })).toBeInTheDocument()
  })

  it('sem detalhe, a linha só tem as células do conteúdo', () => {
    render(
      <Linhas rotulo="Turmas">
        <Linha indice="01">
          <strong role="cell">Redes</strong>
        </Linha>
      </Linhas>,
    )
    expect(within(screen.getByRole('row')).getAllByRole('cell')).toHaveLength(1)
  })

  it('seleciona com clique, Enter e Espaço', async () => {
    const aoSelecionar = vi.fn()
    render(
      <Linhas rotulo="Turmas">
        <Linha indice="01" aoSelecionar={aoSelecionar}>
          <strong role="cell">Redes</strong>
        </Linha>
      </Linhas>,
    )
    const linha = screen.getByRole('row', { name: /Redes/ })

    await userEvent.click(linha)
    linha.focus()
    await userEvent.keyboard('{Enter}')
    await userEvent.keyboard(' ')

    expect(aoSelecionar).toHaveBeenCalledTimes(3)
  })

  it('setas movem o foco entre as linhas selecionáveis', async () => {
    render(
      <Linhas rotulo="Turmas">
        <Linha indice="01" aoSelecionar={() => undefined}>
          <strong role="cell">Redes</strong>
        </Linha>
        <Linha indice="02" aoSelecionar={() => undefined}>
          <strong role="cell">Cálculo</strong>
        </Linha>
      </Linhas>,
    )
    screen.getByRole('row', { name: /Redes/ }).focus()

    await userEvent.keyboard('{ArrowDown}')
    expect(screen.getByRole('row', { name: /Cálculo/ })).toHaveFocus()
    await userEvent.keyboard('{ArrowUp}')
    expect(screen.getByRole('row', { name: /Redes/ })).toHaveFocus()
  })

  it('linha indisponível não recebe foco nem seleciona, e diz o porquê por escrito', async () => {
    const aoSelecionar = vi.fn()
    render(
      <Linhas rotulo="Turmas">
        <Linha indice="01" aoSelecionar={aoSelecionar} indisponivel>
          <strong role="cell">Seminário</strong>
        </Linha>
      </Linhas>,
    )
    const linha = screen.getByRole('row', { name: /Seminário/ })

    await userEvent.click(linha)

    expect(aoSelecionar).not.toHaveBeenCalled()
    expect(linha).toHaveAttribute('aria-disabled', 'true')
    expect(linha).not.toHaveAttribute('tabindex')
  })

  it('marca a linha selecionada para leitores de tela', () => {
    render(
      <Linhas rotulo="Turmas">
        <Linha indice="01" aoSelecionar={() => undefined} selecionada>
          <strong role="cell">Redes</strong>
        </Linha>
      </Linhas>,
    )
    expect(screen.getByRole('row')).toHaveAttribute('aria-selected', 'true')
  })

  it('linha só informativa não é interativa', () => {
    render(
      <Linhas rotulo="Alunos">
        <Linha indice="01">
          <strong role="cell">Ana</strong>
        </Linha>
      </Linhas>,
    )
    expect(screen.getByRole('row')).not.toHaveAttribute('tabindex')
    expect(screen.getByRole('row')).not.toHaveAttribute('aria-selected')
  })
})
