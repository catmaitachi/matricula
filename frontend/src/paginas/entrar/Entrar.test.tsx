import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ErroApi } from '../../api/tipos'
import { apiMock as api } from '../../testes/apiMock'
import { curriculoAberto, minhas } from '../../testes/dados'
import { renderizar } from '../../testes/render'
import { SESSAO_ALUNO } from '../../testes/fixtures'

vi.mock('../../api', async () => (await import('../../testes/apiMock')).moduloApi)

async function abrir() {
  renderizar('/entrar', null)
  await screen.findByRole('form', { name: 'Entrar' })
  return {
    numPessoa: screen.getByLabelText('Nº de pessoa'),
    senha: screen.getByLabelText('Senha'),
    botao: screen.getByRole('button', { name: 'Entrar' }),
  }
}

beforeEach(() => {
  vi.resetAllMocks()
  api.aluno.curriculo.mockResolvedValue(curriculoAberto())
  api.aluno.matriculas.mockResolvedValue(minhas())
})

describe('tela de entrada', () => {
  it('não chama a API com campos vazios e leva o foco ao primeiro inválido', async () => {
    const { numPessoa, botao } = await abrir()

    await userEvent.click(botao)

    expect(api.entrar).not.toHaveBeenCalled()
    expect(numPessoa).toBeInvalid()
    expect(numPessoa).toHaveAccessibleDescription('Informe seu nº de pessoa.')
    expect(screen.getByLabelText('Senha')).toHaveAccessibleDescription('Informe sua senha.')
    expect(numPessoa).toHaveFocus()
  })

  it('foca a senha quando só ela está vazia', async () => {
    const { numPessoa, senha, botao } = await abrir()

    await userEvent.type(numPessoa, 'ALU999')
    await userEvent.click(botao)

    expect(api.entrar).not.toHaveBeenCalled()
    expect(senha).toHaveFocus()
  })

  it('entra (sem espaços nas pontas), confirma quem é e vai para a primeira tela do papel', async () => {
    const { numPessoa, senha, botao } = await abrir()
    api.entrar.mockResolvedValue(undefined)
    api.eu.mockResolvedValue(SESSAO_ALUNO.usuario)

    await userEvent.type(numPessoa, '  ALU999 ')
    await userEvent.type(senha, 'senhaAlu789')
    await userEvent.click(botao)

    expect(api.entrar).toHaveBeenCalledWith('ALU999', 'senhaAlu789')
    expect(await screen.findByRole('heading', { name: 'Currículo' })).toBeInTheDocument()
  })

  it('mostra o erro do servidor, limpa a senha e devolve o foco a ela', async () => {
    const { numPessoa, senha, botao } = await abrir()
    api.entrar.mockRejectedValue(new ErroApi(401, 'CREDENCIAIS_INVALIDAS', 'Nº de pessoa ou senha incorretos.'))

    await userEvent.type(numPessoa, 'ALU999')
    await userEvent.type(senha, 'errada')
    await userEvent.click(botao)

    expect(await within(screen.getByRole('form', { name: 'Entrar' })).findByRole('alert')).toHaveTextContent('Nº de pessoa ou senha incorretos.')
    expect(numPessoa).toHaveValue('ALU999')
    expect(senha).toHaveValue('')
    expect(senha).toHaveFocus()
    expect(botao).toBeEnabled()
  })

  it('explica o bloqueio por excesso de tentativas', async () => {
    const { numPessoa, senha, botao } = await abrir()
    api.entrar.mockRejectedValue(new ErroApi(429, 'MUITAS_TENTATIVAS', 'Muitas tentativas de entrada. Tente de novo em alguns minutos.'))

    await userEvent.type(numPessoa, 'ALU999')
    await userEvent.type(senha, 'x')
    await userEvent.click(botao)

    expect(await within(screen.getByRole('form', { name: 'Entrar' })).findByRole('alert')).toHaveTextContent('Muitas tentativas')
  })

  it('usa uma mensagem neutra para erros que não vêm da API', async () => {
    const { numPessoa, senha, botao } = await abrir()
    api.entrar.mockRejectedValue(new Error('boom'))

    await userEvent.type(numPessoa, 'ALU999')
    await userEvent.type(senha, 'x')
    await userEvent.click(botao)

    expect(await within(screen.getByRole('form', { name: 'Entrar' })).findByRole('alert')).toHaveTextContent('Algo deu errado. Tente de novo.')
  })

  it('bloqueia o botão enquanto envia e não envia duas vezes', async () => {
    const { numPessoa, senha, botao } = await abrir()
    let concluir: () => void = () => {}
    api.entrar.mockReturnValue(new Promise<void>((resolver) => (concluir = resolver)))
    api.eu.mockResolvedValue(SESSAO_ALUNO.usuario)

    await userEvent.type(numPessoa, 'ALU999')
    await userEvent.type(senha, 'senhaAlu789')
    await userEvent.click(botao)

    const enviando = screen.getByRole('button', { name: 'Entrando…' })
    expect(enviando).toBeDisabled()
    expect(enviando).toHaveAttribute('aria-busy', 'true')
    await userEvent.type(senha, '{Enter}')
    expect(api.entrar).toHaveBeenCalledTimes(1)

    concluir()
    expect(await screen.findByRole('heading', { name: 'Currículo' })).toBeInTheDocument()
  })
})
