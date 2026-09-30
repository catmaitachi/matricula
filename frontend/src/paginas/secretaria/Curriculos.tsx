import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../../api'
import { ROTULO_ESTADO_CURRICULO } from '../../api/rotulos'
import { Plus } from 'lucide-react'
import { Botao } from '../../componentes/Botao'
import { Icone } from '../../componentes/Icone'
import { Campo } from '../../componentes/Campo'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas, Marca } from '../../componentes/Linhas'
import { useAviso } from '../../componentes/contextoAviso'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina } from '../../layout/Pagina'
import { camposDe, mensagemDe, pad2 } from '../../util'

/** RF01: os currículos por semestre. Um novo nasce em rascunho, para a secretaria montar antes de abrir. */
export function Curriculos() {
  const { avisar } = useAviso()
  const navegar = useNavigate()
  const [carga, recarregar] = useCarregar(() => api.curriculos.listar())
  const [criando, setCriando] = useState(false)
  const [semestre, setSemestre] = useState('')
  const [erro, setErro] = useState<string>()
  const [enviando, setEnviando] = useState(false)

  async function criar(e: FormEvent) {
    e.preventDefault()
    setEnviando(true)
    setErro(undefined)
    try {
      const novo = await api.curriculos.criar(semestre)
      avisar('ok', `Currículo ${novo.semestre} criado. Monte as turmas e abra as inscrições.`)
      navegar(`/secretaria/curriculos/${novo.id}`)
    } catch (e) {
      setErro(camposDe(e).semestre ?? mensagemDe(e))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Pagina kicker="Secretaria" titulo="Currículos" acoes={<Botao onClick={() => setCriando(true)}><Icone de={Plus} size={18} /> Novo semestre</Botao>}>
      {criando && (
        <form className={formulario.formulario} onSubmit={(e) => void criar(e)} noValidate aria-label="Novo currículo">
          <Campo rotulo="Semestre (ex.: 2026/2)" value={semestre} onChange={(e) => setSemestre(e.target.value)} erro={erro} required autoComplete="off" />
          <div className={formulario.acoes}>
            <Botao type="submit" carregando={enviando}>
              {enviando ? 'Criando…' : 'Criar'}
            </Botao>
            <Botao onClick={() => setCriando(false)}>Cancelar</Botao>
          </div>
        </form>
      )}
      <Carga carga={carga} tentar={recarregar}>
        {(curriculos) =>
          curriculos.length === 0 ? (
            <p className={formulario.vazio}>Nenhum currículo ainda. Crie o primeiro semestre para começar.</p>
          ) : (
            <Linhas rotulo="Currículos">
              {curriculos.map((c, i) => (
                <Linha key={c.id} indice={pad2(i + 1)} aoSelecionar={() => navegar(`/secretaria/curriculos/${c.id}`)}>
                  <strong role="cell">
                    {c.semestre}
                    <Marca>{ROTULO_ESTADO_CURRICULO[c.estado]}</Marca>
                  </strong>
                  <span role="cell" data-fim className={formulario.fim}>
                    {c.turmas} turmas
                  </span>
                </Linha>
              ))}
            </Linhas>
          )
        }
      </Carga>
    </Pagina>
  )
}
