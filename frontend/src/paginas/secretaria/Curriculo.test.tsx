import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { conta, curriculoDetalhe, disciplina } from '../../testes/dados'
import { SESSAO_SECRETARIA } from '../../testes/fixtures'
import { renderizar } from '../../testes/render'
import { segurar } from '../../testes/segurar'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

const TURMA = {
  id: 20, disciplinaId: 5, disciplina: 'Redes de Computadores', codigo: 'A', turno: 'NOITE' as const, professorId: 8,
  professor: 'Dr. Carlos', estado: 'ABERTA' as const, ocupadas: 2, minAlunos: 3, maxAlunos: 60,
}

async function abrir(curriculo = curriculoDetalhe()) {
  api.curriculos.detalhe.mockResolvedValue(curriculo)
  api.disciplinas.listar.mockResolvedValue([disciplina(), disciplina({ id: 6, nome: 'Antiga', ativa: false })])
  api.contas.listar.mockResolvedValue([
    conta({ id: 8, nome: 'Dr. Carlos', papel: 'PROFESSOR', numMatricula: null }),
    conta({ id: 9, nome: 'Afastado', papel: 'PROFESSOR', numMatricula: null, ativo: false }),
  ])
  renderizar('/secretaria/curriculos/9', SESSAO_SECRETARIA.usuario)
  await screen.findByRole('heading', { name: curriculo.semestre })
}

beforeEach(() => vi.resetAllMocks())

describe('currículo em detalhe', () => {
  it('em rascunho: monta uma turma escolhendo só disciplinas e professores ativos', async () => {
    await abrir()
    api.curriculos.adicionarTurma.mockResolvedValue(TURMA)

    const form = screen.getByRole('form', { name: 'Adicionar turma' })
    expect(within(form).queryByRole('option', { name: 'Antiga' })).not.toBeInTheDocument()
    expect(within(form).queryByRole('option', { name: 'Afastado' })).not.toBeInTheDocument()
    expect(within(form).getByRole('button', { name: 'Adicionar' })).toBeDisabled()

    await userEvent.selectOptions(within(form).getByLabelText('Disciplina'), 'Redes de Computadores')
    await userEvent.selectOptions(within(form).getByLabelText('Professor'), 'Dr. Carlos')
    await userEvent.selectOptions(within(form).getByLabelText('Turno'), 'Noite')
    await userEvent.click(within(form).getByRole('button', { name: 'Adicionar' }))

    expect(api.curriculos.adicionarTurma).toHaveBeenCalledWith(9, { disciplinaId: 5, professorId: 8, turno: 'NOITE' })
    expect(await screen.findByText('Turma adicionada.')).toBeInTheDocument()
  })

  it('não dá para abrir as inscrições sem turmas; com turmas, abre', async () => {
    await abrir()
    expect(screen.getByRole('button', { name: 'Abrir inscrições' })).toBeDisabled()
  })

  it('abre as inscrições quando há turmas', async () => {
    await abrir(curriculoDetalhe({ turmas: [TURMA] }))
    api.curriculos.abrir.mockResolvedValue({ id: 9, semestre: '2026/2', estado: 'ABERTO', turmas: 1 })

    await userEvent.click(screen.getByRole('button', { name: 'Abrir inscrições' }))

    expect(api.curriculos.abrir).toHaveBeenCalledWith(9)
    expect(await screen.findByText('Inscrições de 2026/2 abertas.')).toBeInTheDocument()
  })

  it('mostra o motivo se outro semestre ainda está aberto', async () => {
    await abrir(curriculoDetalhe({ turmas: [TURMA] }))
    api.curriculos.abrir.mockRejectedValue(new ErroApi(409, 'JA_HA_CURRICULO_ABERTO', 'O semestre 2026/1 ainda está com inscrições abertas. Encerre-o antes.'))

    await userEvent.click(screen.getByRole('button', { name: 'Abrir inscrições' }))

    expect(await screen.findByText(/2026\/1 ainda está com inscrições abertas/)).toBeInTheDocument()
  })

  it('remove uma turma enquanto é rascunho', async () => {
    await abrir(curriculoDetalhe({ turmas: [TURMA] }))
    api.curriculos.removerTurma.mockResolvedValue(undefined)

    await userEvent.click(screen.getByRole('button', { name: 'Remover' }))

    expect(api.curriculos.removerTurma).toHaveBeenCalledWith(9, 20)
  })

  it('aberto: não edita mais; encerra segurando o botão e conta o que caiu', async () => {
    await abrir(curriculoDetalhe({ estado: 'ABERTO', turmas: [TURMA] }))
    api.curriculos.encerrar.mockResolvedValue({ turmasCanceladas: 1, matriculasCanceladas: 2 })

    expect(screen.queryByRole('form', { name: 'Adicionar turma' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Remover' })).not.toBeInTheDocument()
    const botao = screen.getByRole('button', { name: /Segure para encerrar/ })

    segurar(botao, 800)
    expect(api.curriculos.encerrar).not.toHaveBeenCalled()
    segurar(botao, 1700)

    expect(api.curriculos.encerrar).toHaveBeenCalledWith(9)
    expect(await screen.findByText(/1 turma\(s\) e 2 matrícula\(s\) canceladas por falta de quórum/)).toBeInTheDocument()
  })

  it('encerrado: mostra o resultado por turma e nenhuma ação', async () => {
    await abrir(curriculoDetalhe({ estado: 'ENCERRADO', turmas: [TURMA, { ...TURMA, id: 21, codigo: 'B', estado: 'CANCELADA' }] }))

    expect(screen.getByRole('row', { name: /Turma B/ })).toHaveTextContent('cancelada')
    expect(screen.queryByRole('button', { name: /Abrir|Segure|Remover/ })).not.toBeInTheDocument()
  })
})
