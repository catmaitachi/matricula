import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { apiMock as api } from './testes/apiMock'
import { curriculoAberto, curriculoDetalhe, minhas, turmaProfessor } from './testes/dados'
import { SESSAO_ALUNO, SESSAO_PROFESSOR, SESSAO_SECRETARIA } from './testes/fixtures'
import { renderizar } from './testes/render'

vi.mock('./api', async () => (await import('./testes/apiMock')).moduloApi)

beforeEach(() => {
  vi.resetAllMocks()
  api.aluno.curriculo.mockResolvedValue(curriculoAberto())
  api.aluno.matriculas.mockResolvedValue(minhas())
  api.professor.turmas.mockResolvedValue([turmaProfessor()])
  api.contas.listar.mockResolvedValue([])
  api.curriculos.listar.mockResolvedValue([])
  api.curriculos.detalhe.mockResolvedValue(curriculoDetalhe())
  api.sair.mockResolvedValue(undefined)
})

describe('rotas', () => {
  it('leva quem não entrou para a tela de entrada', async () => {
    renderizar('/', null)
    expect(await screen.findByRole('form', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('guarda o lugar pedido? Não: visitante em qualquer rota cai na entrada', async () => {
    renderizar('/secretaria/contas', null)
    expect(await screen.findByRole('form', { name: 'Entrar' })).toBeInTheDocument()
  })

  it.each([
    [SESSAO_ALUNO, 'Currículo'],
    [SESSAO_PROFESSOR, 'Minhas turmas'],
    [SESSAO_SECRETARIA, 'Contas'],
  ])('a raiz leva cada papel à sua primeira tela', async (sessao, titulo) => {
    renderizar('/', sessao.usuario)
    expect(await screen.findByRole('heading', { name: titulo })).toBeInTheDocument()
  })

  it('quem já entrou não vê a tela de entrada', async () => {
    renderizar('/entrar', SESSAO_ALUNO.usuario)
    expect(await screen.findByRole('heading', { name: 'Currículo' })).toBeInTheDocument()
  })

  it('cada papel só abre as suas telas', async () => {
    renderizar('/secretaria/contas', SESSAO_ALUNO.usuario)
    expect(await screen.findByRole('heading', { name: 'Currículo' })).toBeInTheDocument()
    expect(api.contas.listar).not.toHaveBeenCalled()
  })

  it('rota desconhecida vai para o início do papel', async () => {
    renderizar('/nao-existe', SESSAO_PROFESSOR.usuario)
    expect(await screen.findByRole('heading', { name: 'Minhas turmas' })).toBeInTheDocument()
  })

  it('o menu mostra só o que o papel usa e marca a página atual', async () => {
    renderizar('/', SESSAO_SECRETARIA.usuario)
    await screen.findByRole('heading', { name: 'Contas' })

    const menu = screen.getByRole('navigation', { name: 'Principal' })
    expect([...menu.querySelectorAll('a')].map((a) => a.getAttribute('href'))).toEqual(['/secretaria/contas', '/secretaria/disciplinas', '/secretaria/curriculos'])
    expect(screen.getByRole('link', { name: /Contas/ })).toHaveAttribute('aria-current', 'page')
  })

  it('o trilho tem um botão que fixa o menu aberto e avisa o estado', async () => {
    renderizar('/', SESSAO_ALUNO.usuario)
    const fixar = await screen.findByRole('button', { name: 'Fixar menu aberto' })
    expect(fixar).toHaveAttribute('aria-pressed', 'false')

    await userEvent.click(fixar)
    expect(fixar).toHaveAttribute('aria-pressed', 'true')
  })

  it('o título da tela é um só cabeçalho legível (as letras animadas ficam escondidas)', async () => {
    renderizar('/', SESSAO_SECRETARIA.usuario)
    const titulo = await screen.findByRole('heading', { level: 1, name: 'Contas' })
    expect(titulo.querySelector('[aria-hidden="true"]')).not.toBeNull()
    expect(screen.getAllByRole('heading', { level: 1 })).toHaveLength(1)
  })

  it('o trilho tem o botão de tema, que mostra qual está valendo', async () => {
    renderizar('/', SESSAO_ALUNO.usuario)
    const botao = await screen.findByRole('button', { name: /Tema: automático/ })

    await userEvent.click(botao)
    expect(screen.getByRole('button', { name: /Tema: claro/ })).toBeInTheDocument()
  })

  it('o nome de quem está logado fica numa linha própria, cortado com reticências e completo no title', async () => {
    renderizar('/', SESSAO_ALUNO.usuario)
    const sair = await screen.findByRole('button', { name: 'Sair' })
    expect(within(sair).getByTitle(SESSAO_ALUNO.usuario.nome)).toBeInTheDocument()
  })

  it('no celular o menu é um hambúrguer: abre, avisa o estado, fecha com Esc e ao navegar', async () => {
    renderizar('/aluno/curriculo', SESSAO_ALUNO.usuario)
    const abrir = await screen.findByRole('button', { name: 'Menu', expanded: false })

    await userEvent.click(abrir)
    expect(screen.getByRole('button', { name: 'Menu', expanded: true })).toBeInTheDocument()

    await userEvent.keyboard('{Escape}')
    expect(screen.getByRole('button', { name: 'Menu', expanded: false })).toBeInTheDocument()

    await userEvent.click(abrir)
    await userEvent.click(screen.getByRole('link', { name: /Minhas matrículas/ }))
    expect(await screen.findByRole('button', { name: 'Menu', expanded: false })).toBeInTheDocument()
  })

  it('sair encerra a sessão e volta para a entrada', async () => {
    renderizar('/', SESSAO_ALUNO.usuario)
    await userEvent.click(await screen.findByRole('button', { name: 'Sair' }))

    expect(await screen.findByRole('form', { name: 'Entrar' })).toBeInTheDocument()
    expect(api.sair).toHaveBeenCalled()
  })
})
