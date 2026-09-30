import { fireEvent, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { apiMock as api } from '../../testes/apiMock'
import { disciplina } from '../../testes/dados'
import { SESSAO_SECRETARIA } from '../../testes/fixtures'
import { renderizar } from '../../testes/render'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

const CURSOS = [
  { id: 1, nome: 'Engenharia de Software', numCreditos: 240 },
  { id: 2, nome: 'Ciência da Computação', numCreditos: 240 },
]

async function abrir(disciplinas = [disciplina()]) {
  api.cursos.listar.mockResolvedValue(CURSOS)
  api.disciplinas.listar.mockResolvedValue(disciplinas)
  renderizar('/secretaria/disciplinas', SESSAO_SECRETARIA.usuario)
  await screen.findByRole('button', { name: 'Nova disciplina' }) // só existe depois que os dados chegam
}

beforeEach(() => vi.resetAllMocks())

describe('disciplinas e cursos', () => {
  it('lista cada disciplina com seus cursos e limites de alunos', async () => {
    await abrir()
    const linha = await screen.findByRole('row', { name: /Redes de Computadores/ })
    expect(linha).toHaveTextContent('Engenharia de Software')
    expect(linha).toHaveTextContent('3 a 60 alunos')
  })

  it('cria uma disciplina com os limites padrão (3 e 60) e os cursos marcados', async () => {
    await abrir([])
    api.disciplinas.criar.mockResolvedValue(disciplina())

    await userEvent.click(screen.getByRole('button', { name: 'Nova disciplina' }))
    const form = screen.getByRole('form', { name: 'Nova disciplina' })
    await userEvent.type(within(form).getByLabelText('Nome da disciplina'), 'Banco de Dados')
    await userEvent.click(within(form).getByRole('checkbox', { name: 'Ciência da Computação' }))
    await userEvent.click(within(form).getByRole('button', { name: 'Salvar' }))

    expect(api.disciplinas.criar).toHaveBeenCalledWith({ nome: 'Banco de Dados', minAlunos: 3, maxAlunos: 60, ativa: true, cursoIds: [2] })
    expect(await screen.findByText('Disciplina Banco de Dados salva.')).toBeInTheDocument()
  })

  it('a régua deixa escolher qualquer valor e mostra o padrão como referência', async () => {
    await abrir([])
    await userEvent.click(screen.getByRole('button', { name: 'Nova disciplina' }))
    const form = screen.getByRole('form', { name: 'Nova disciplina' })

    expect(within(form).getByText('padrão 3')).toBeInTheDocument()
    expect(within(form).getByText('padrão 60')).toBeInTheDocument()
    fireEvent.change(within(form).getByRole('slider', { name: 'Máximo de alunos' }), { target: { value: '25' } })
    expect(within(form).getByRole('slider', { name: 'Máximo de alunos' })).toHaveValue('25')
  })

  it('não deixa salvar com mínimo maior que o máximo, antes de chamar a API', async () => {
    await abrir([])
    await userEvent.click(screen.getByRole('button', { name: 'Nova disciplina' }))
    const form = screen.getByRole('form', { name: 'Nova disciplina' })
    await userEvent.type(within(form).getByLabelText('Nome da disciplina'), 'X')
    fireEvent.change(within(form).getByRole('slider', { name: 'Mínimo de alunos' }), { target: { value: '20' } })
    fireEvent.change(within(form).getByRole('slider', { name: 'Máximo de alunos' }), { target: { value: '10' } })
    await userEvent.click(within(form).getByRole('button', { name: 'Salvar' }))

    expect(await within(form).findByText(/mínimo de alunos não pode passar do máximo/)).toBeInTheDocument()
    expect(api.disciplinas.criar).not.toHaveBeenCalled()
  })

  it('edita uma disciplina existente com os valores dela', async () => {
    await abrir([disciplina({ maxAlunos: 40 })])
    api.disciplinas.atualizar.mockResolvedValue(disciplina())

    await userEvent.click(await screen.findByRole('row', { name: /Redes de Computadores/ }))
    const form = screen.getByRole('form', { name: 'Editar disciplina' })
    expect(within(form).getByLabelText('Nome da disciplina')).toHaveValue('Redes de Computadores')
    expect(within(form).getByRole('slider', { name: 'Máximo de alunos' })).toHaveValue('40')
    expect(within(form).getByRole('checkbox', { name: 'Engenharia de Software' })).toBeChecked()
    await userEvent.click(within(form).getByRole('button', { name: 'Salvar' }))

    expect(api.disciplinas.atualizar).toHaveBeenCalledWith(5, expect.objectContaining({ maxAlunos: 40, cursoIds: [1] }))
  })

  it('cria um curso', async () => {
    await abrir()
    api.cursos.criar.mockResolvedValue(CURSOS[0]!)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo curso' }))
    const form = screen.getByRole('form', { name: 'Novo curso' })
    await userEvent.type(within(form).getByLabelText('Nome do curso'), 'Sistemas de Informação')
    await userEvent.type(within(form).getByLabelText('Créditos'), '220')
    await userEvent.click(within(form).getByRole('button', { name: 'Salvar' }))

    expect(api.cursos.criar).toHaveBeenCalledWith({ nome: 'Sistemas de Informação', numCreditos: 220 })
  })
})
