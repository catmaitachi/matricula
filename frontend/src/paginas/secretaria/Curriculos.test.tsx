import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { curriculoDetalhe } from '../../testes/dados'
import { SESSAO_SECRETARIA } from '../../testes/fixtures'
import { renderizar } from '../../testes/render'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

beforeEach(() => {
  vi.resetAllMocks()
  api.curriculos.detalhe.mockResolvedValue(curriculoDetalhe())
  api.disciplinas.listar.mockResolvedValue([])
  api.contas.listar.mockResolvedValue([])
})

describe('currículos', () => {
  it('lista os semestres com o estado escrito e abre o escolhido', async () => {
    api.curriculos.listar.mockResolvedValue([
      { id: 9, semestre: '2026/2', estado: 'ABERTO', turmas: 8 },
      { id: 8, semestre: '2026/1', estado: 'ENCERRADO', turmas: 6 },
    ])
    renderizar('/secretaria/curriculos', SESSAO_SECRETARIA.usuario)

    const linha = await screen.findByRole('row', { name: /2026\/2/ })
    expect(linha).toHaveTextContent('inscrições abertas')
    expect(linha).toHaveTextContent('8 turmas')
    expect(screen.getByRole('row', { name: /2026\/1/ })).toHaveTextContent('encerrado')

    await userEvent.click(linha)
    expect(await screen.findByRole('heading', { name: '2026/2' })).toBeInTheDocument()
    expect(api.curriculos.detalhe).toHaveBeenCalledWith(9)
  })

  it('cria um semestre em rascunho e vai para a tela dele', async () => {
    api.curriculos.listar.mockResolvedValue([])
    api.curriculos.criar.mockResolvedValue({ id: 9, semestre: '2027/1', estado: 'RASCUNHO', turmas: 0 })
    api.curriculos.detalhe.mockResolvedValue(curriculoDetalhe({ semestre: '2027/1' }))
    renderizar('/secretaria/curriculos', SESSAO_SECRETARIA.usuario)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo semestre' }))
    await userEvent.type(screen.getByLabelText(/Semestre/), '2027/1')
    await userEvent.click(screen.getByRole('button', { name: 'Criar' }))

    expect(api.curriculos.criar).toHaveBeenCalledWith('2027/1')
    expect(await screen.findByRole('heading', { name: '2027/1' })).toBeInTheDocument()
  })

  it('mostra o erro de semestre inválido no próprio campo', async () => {
    api.curriculos.listar.mockResolvedValue([])
    api.curriculos.criar.mockRejectedValue(new ErroApi(422, 'SEMESTRE_INVALIDO', 'Informe o semestre no formato AAAA/1 ou AAAA/2.'))
    renderizar('/secretaria/curriculos', SESSAO_SECRETARIA.usuario)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo semestre' }))
    await userEvent.type(screen.getByLabelText(/Semestre/), '2027-1')
    await userEvent.click(screen.getByRole('button', { name: 'Criar' }))

    expect(await screen.findByText(/formato AAAA\/1 ou AAAA\/2/)).toBeInTheDocument()
    expect(screen.getByLabelText(/Semestre/)).toBeInvalid()
  })
})
