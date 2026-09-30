import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { conta } from '../../testes/dados'
import { SESSAO_SECRETARIA } from '../../testes/fixtures'
import { renderizar } from '../../testes/render'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

async function abrir(contas = [conta()]) {
  api.contas.listar.mockResolvedValue(contas)
  renderizar('/secretaria/contas', SESSAO_SECRETARIA.usuario)
  await screen.findByRole('heading', { name: 'Contas' })
}

beforeEach(() => vi.resetAllMocks())

describe('contas', () => {
  it('lista os alunos e troca para professores', async () => {
    await abrir()
    expect(await screen.findByRole('row', { name: /Ana Souza/ })).toHaveTextContent('Matrícula 2026001')
    expect(api.contas.listar).toHaveBeenLastCalledWith('ALUNO')

    api.contas.listar.mockResolvedValue([conta({ id: 8, nome: 'Dr. Carlos', numPessoa: 'PROF1', papel: 'PROFESSOR', numMatricula: null })])
    await userEvent.click(screen.getByRole('radio', { name: 'Professores' }))

    expect(await screen.findByRole('row', { name: /Dr\. Carlos/ })).toBeInTheDocument()
    expect(api.contas.listar).toHaveBeenLastCalledWith('PROFESSOR')
  })

  it('cria um aluno e avisa; a senha vai só no envio', async () => {
    await abrir([])
    api.contas.criar.mockResolvedValue(conta())

    await userEvent.click(screen.getByRole('button', { name: 'Nova conta' }))
    const form = screen.getByRole('form', { name: 'Nova conta' })
    await userEvent.type(within(form).getByLabelText('Nº de pessoa'), 'ALU1')
    await userEvent.type(within(form).getByLabelText('Nome'), 'Ana Souza')
    await userEvent.type(within(form).getByLabelText('Nº de matrícula'), '2026001')
    await userEvent.type(within(form).getByLabelText(/Senha inicial/), 'senha-forte-1')
    await userEvent.click(within(form).getByRole('button', { name: 'Salvar' }))

    expect(api.contas.criar).toHaveBeenCalledWith({
      papel: 'ALUNO', numPessoa: 'ALU1', nome: 'Ana Souza', numMatricula: '2026001', senha: 'senha-forte-1',
    })
    expect(await screen.findByText('Conta de Ana Souza criada.')).toBeInTheDocument()
    expect(screen.queryByRole('form', { name: 'Nova conta' })).not.toBeInTheDocument()
  })

  it('o campo de senha é de senha (não mostra o que se digita)', async () => {
    await abrir([])
    await userEvent.click(screen.getByRole('button', { name: 'Nova conta' }))
    expect(screen.getByLabelText(/Senha inicial/)).toHaveAttribute('type', 'password')
    expect(screen.getByLabelText(/Senha inicial/)).toHaveAttribute('autocomplete', 'new-password')
  })

  it('mostra o erro de cada campo devolvido pela API, sem fechar o formulário', async () => {
    await abrir([])
    api.contas.criar.mockRejectedValue(
      new ErroApi(400, 'DADO_INVALIDO', 'Confira os campos informados.', { senha: 'tamanho deve ser entre 8 e 72', numPessoa: 'use só letras' }),
    )

    await userEvent.click(screen.getByRole('button', { name: 'Nova conta' }))
    await userEvent.click(within(screen.getByRole('form', { name: 'Nova conta' })).getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByText('tamanho deve ser entre 8 e 72')).toBeInTheDocument()
    expect(screen.getByLabelText(/Senha inicial/)).toBeInvalid()
    expect(screen.getByLabelText('Nº de pessoa')).toHaveAccessibleDescription('use só letras')
    expect(screen.getByRole('form', { name: 'Nova conta' })).toBeInTheDocument()
  })

  it('erro que não é de campo (ex.: nº de pessoa já usado) vira aviso', async () => {
    await abrir([])
    api.contas.criar.mockRejectedValue(new ErroApi(409, 'NUM_PESSOA_EM_USO', 'Já existe uma conta com este nº de pessoa.'))

    await userEvent.click(screen.getByRole('button', { name: 'Nova conta' }))
    await userEvent.click(within(screen.getByRole('form', { name: 'Nova conta' })).getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByText('Já existe uma conta com este nº de pessoa.')).toBeInTheDocument()
  })

  it('edita: desativa a conta e só troca a senha se uma nova for digitada', async () => {
    await abrir()
    api.contas.atualizar.mockResolvedValue(conta({ ativo: false }))

    await userEvent.click(await screen.findByRole('row', { name: /Ana Souza/ }))
    const form = screen.getByRole('form', { name: 'Editar conta' })
    expect(within(form).getByLabelText('Nome')).toHaveValue('Ana Souza')
    expect(within(form).queryByLabelText('Nº de pessoa')).not.toBeInTheDocument() // o número de pessoa não muda
    await userEvent.click(within(form).getByRole('radio', { name: 'Desativada' }))
    await userEvent.click(within(form).getByRole('button', { name: 'Salvar' }))

    expect(api.contas.atualizar).toHaveBeenCalledWith(3, { nome: 'Ana Souza', ativo: false, numMatricula: '2026001', novaSenha: undefined })
    expect(await screen.findByText('Conta de Ana Souza atualizada.')).toBeInTheDocument()
  })

  it('conta desativada aparece marcada por escrito', async () => {
    await abrir([conta({ ativo: false })])
    expect(await screen.findByRole('row', { name: /Ana Souza/ })).toHaveTextContent('desativada')
  })
})
