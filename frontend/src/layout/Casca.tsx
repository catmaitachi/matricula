import { useEffect, useRef, useState, type MouseEvent } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { LogOut, Menu, Monitor, Moon, Sun, X } from 'lucide-react'
import { Icone } from '../componentes/Icone'
import { pad2 } from '../util'
import { useAuth } from '../auth/contexto'
import { NAVEGACAO } from './navegacao'
import { useTema, type Tema } from './tema'
import estilos from './Casca.module.css'

const ROTULO_TEMA: Record<Tema, string> = { auto: 'automático', claro: 'claro', escuro: 'escuro' }
const ICONE_TEMA = { auto: Monitor, claro: Sun, escuro: Moon }

const reduzMovimento = () => typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches

/**
 * Cortina de bloco entre telas: cobre de cima para baixo (170 ms), a tela troca por trás e a cortina sai por baixo (170 ms).
 * Sem Web Animations ou com movimento reduzido, é um corte direto.
 */
function useCortina() {
  const cortina = useRef<HTMLDivElement>(null)
  const navegar = useNavigate()
  const { pathname } = useLocation()
  const cobrindo = useRef(false)

  const animavel = () => !reduzMovimento() && typeof cortina.current?.animate === 'function'

  async function ir(para: string) {
    if (para === pathname) return
    const el = cortina.current
    if (!el || !animavel()) return navegar(para)
    cobrindo.current = true
    await el.animate([{ clipPath: 'inset(0 0 100% 0)' }, { clipPath: 'inset(0)' }], { duration: 170, easing: 'cubic-bezier(.7,0,.2,1)', fill: 'forwards' }).finished
    navegar(para)
  }

  useEffect(() => {
    const el = cortina.current
    document.getElementById('conteudo')?.focus({ preventScroll: true })
    if (!cobrindo.current || !el || typeof el.animate !== 'function') return
    cobrindo.current = false
    void el
      .animate([{ clipPath: 'inset(0)' }, { clipPath: 'inset(100% 0 0 0)' }], { duration: 170, easing: 'cubic-bezier(.7,0,.2,1)', fill: 'forwards' })
      .finished.then(() => el.getAnimations().forEach((a) => a.cancel()))
  }, [pathname])

  return { cortina, ir }
}

/** Trilho de bloco: 80px só com os números; abre no cursor, no foco ou fixado pelo botão ≡. */
export function Casca() {
  const { usuario, sair } = useAuth()
  const [fixa, setFixa] = useState(false)
  const { cortina, ir } = useCortina()
  const { tema, alternar } = useTema()
  const { pathname } = useLocation()
  // celular: o trilho vira uma gaveta aberta pelo botão hambúrguer (no desktop estes estados não fazem nada)
  // guarda em qual rota foi aberto: navegar fecha o menu sem efeito
  const [abertoEm, setAbertoEm] = useState<string | null>(null)
  const aberto = abertoEm === pathname
  const hamburguer = useRef<HTMLButtonElement>(null)
  const trilho = useRef<HTMLElement>(null)

  useEffect(() => {
    if (!aberto) return
    trilho.current?.querySelector<HTMLElement>('a')?.focus()
    const aoTeclar = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return
      setAbertoEm(null)
      hamburguer.current?.focus()
    }
    window.addEventListener('keydown', aoTeclar)
    return () => window.removeEventListener('keydown', aoTeclar)
  }, [aberto])

  if (!usuario) return null

  const fechar = () => {
    setAbertoEm(null)
    hamburguer.current?.focus()
  }

  const aoClicar = (para: string) => (e: MouseEvent) => {
    if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || e.button !== 0) return
    e.preventDefault()
    void ir(para)
  }

  return (
    <>
      <a className={estilos.pular} href="#conteudo">
        Ir para o conteúdo
      </a>
      <div className={estilos.topo}>
        <button ref={hamburguer} type="button" className={estilos.hamburguer} aria-expanded={aberto} aria-controls="menu-principal" onClick={() => setAbertoEm(pathname)}>
          <Icone de={Menu} />
          Menu
        </button>
        <span className={estilos.marca}>Matrícula</span>
      </div>
      <div className={`${estilos.veu} ${aberto ? estilos.veuAberto : ''}`} aria-hidden="true" onClick={fechar} />
      <nav
        ref={trilho}
        id="menu-principal"
        className={`${estilos.trilho} ${fixa ? estilos.fixa : ''} ${aberto ? estilos.aberto : ''}`}
        aria-label="Principal"
      >
        <button type="button" className={`${estilos.item} ${estilos.soCelular}`} onClick={fechar}>
          <span className={estilos.icone}>
            <Icone de={X} />
          </span>
          <span className={estilos.texto}>Fechar menu</span>
        </button>
        <button type="button" className={`${estilos.item} ${estilos.soDesktop}`} aria-pressed={fixa} aria-label="Fixar menu aberto" onClick={() => setFixa(!fixa)}>
          <span className={estilos.icone}>
            <Icone de={Menu} />
          </span>
          <span className={estilos.texto} aria-hidden="true">
            Menu
          </span>
        </button>
        {NAVEGACAO[usuario.papel].map((item, i) => (
          <NavLink key={item.para} to={item.para} onClick={aoClicar(item.para)} className={estilos.item}>
            <span className={estilos.icone}>
              <Icone de={item.icone} />
            </span>
            <span className={estilos.texto}>{item.rotulo}</span>
            <span className={`${estilos.texto} ${estilos.numero}`} aria-hidden="true">
              {pad2(i + 1)}
            </span>
          </NavLink>
        ))}
        <span className={estilos.folga} />
        <button type="button" className={estilos.item} onClick={alternar}>
          <span className={estilos.icone}>
            <Icone de={ICONE_TEMA[tema]} />
          </span>
          <span className={estilos.texto}>Tema: {ROTULO_TEMA[tema]}</span>
        </button>
        <button type="button" className={estilos.item} onClick={() => void sair()}>
          <span className={estilos.icone}>
            <Icone de={LogOut} />
          </span>
          <span className={estilos.texto}>
            Sair
            <span className={estilos.nome} aria-hidden="true" title={usuario.nome}>
              {usuario.nome}
            </span>
          </span>
        </button>
      </nav>
      <main id="conteudo" tabIndex={-1} className={estilos.principal}>
        <Outlet />
      </main>
      <div ref={cortina} className={estilos.cortina} aria-hidden="true" />
    </>
  )
}
