import { Fragment, type CSSProperties, type ReactNode } from 'react'
import { ROTULO_PAPEL } from '../api/rotulos'
import { useAuth } from '../auth/contexto'
import estilos from './Pagina.module.css'

interface Props {
  kicker: string
  titulo: string
  acoes?: ReactNode
  /** Números que a tela precisa mostrar de relance (limites, contagens): ficam dentro da cena, sob o título. */
  registros?: ReactNode
  children: ReactNode
}

/** O título é texto real (sr-only); as letras animadas são só decoração e ficam escondidas do leitor de tela. */
function Titulo({ texto }: { texto: string }) {
  let i = 0
  return (
    <h1 className={estilos.titulo}>
      <span className={estilos.so}>{texto}</span>
      <span aria-hidden="true">
        {texto.split(' ').map((palavra, k, todas) => (
          <Fragment key={k}>
            <span className={estilos.palavra}>
              {[...palavra].map((letra, j) => (
                <span key={j} className={estilos.letra} style={{ '--i': i++ } as CSSProperties}>
                  {letra}
                </span>
              ))}
            </span>
            {k < todas.length - 1 && ' '}
          </Fragment>
        ))}
      </span>
    </h1>
  )
}

/** Cena de cada tela: linha de contexto, título gigante que dobra e, se houver, os registros, com grão e uma linha grossa embaixo. */
export function Pagina({ kicker, titulo, acoes, registros, children }: Props) {
  const { usuario } = useAuth()
  return (
    <section className={estilos.pagina}>
      <title>{`${titulo} · Matrícula`}</title>
      <header className={estilos.cena}>
        <div className={estilos.meta}>
          <span className={estilos.kicker}>{kicker}</span>
          {usuario && (
            <span>
              {usuario.nome} · {ROTULO_PAPEL[usuario.papel]}
            </span>
          )}
        </div>
        <Titulo texto={titulo} />
        {acoes && <div className={estilos.acoes}>{acoes}</div>}
        {registros && <div className={estilos.registros}>{registros}</div>}
      </header>
      <div className={estilos.corpo}>{children}</div>
    </section>
  )
}

/** Bloco com título menor dentro de uma tela. */
export function Secao({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className={estilos.secao}>
      <h2 className={estilos.subtitulo}>{titulo}</h2>
      {children}
    </section>
  )
}
