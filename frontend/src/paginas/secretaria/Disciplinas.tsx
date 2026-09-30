import { Fragment, useState, type FormEvent } from 'react'
import { api } from '../../api'
import type { Curso, Disciplina } from '../../api/tipos'
import { Plus } from 'lucide-react'
import { Botao } from '../../componentes/Botao'
import { Icone } from '../../componentes/Icone'
import { Campo } from '../../componentes/Campo'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas, Marca, Painel } from '../../componentes/Linhas'
import { Opcoes } from '../../componentes/Opcoes'
import { Regua } from '../../componentes/Regua'
import { useAviso } from '../../componentes/contextoAviso'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina, Secao } from '../../layout/Pagina'
import { camposDe, mensagemDe, pad2 } from '../../util'

const MINIMO_PADRAO = 3
const MAXIMO_PADRAO = 60

function FormularioCurso({ aoConcluir }: { aoConcluir: () => void }) {
  const { avisar } = useAviso()
  const [nome, setNome] = useState('')
  const [creditos, setCreditos] = useState('')
  const [erros, setErros] = useState<Record<string, string>>({})
  const [enviando, setEnviando] = useState(false)

  async function salvar(e: FormEvent) {
    e.preventDefault()
    setEnviando(true)
    setErros({})
    try {
      await api.cursos.criar({ nome, numCreditos: Number(creditos) })
      avisar('ok', `Curso ${nome} criado.`)
      aoConcluir()
    } catch (erro) {
      const campos = camposDe(erro)
      if (Object.keys(campos).length > 0) setErros(campos)
      else avisar('erro', mensagemDe(erro))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className={formulario.formulario} onSubmit={(e) => void salvar(e)} noValidate aria-label="Novo curso">
      <div className={formulario.colunas}>
        <Campo rotulo="Nome do curso" value={nome} onChange={(e) => setNome(e.target.value)} erro={erros.nome} required />
        <Campo rotulo="Créditos" type="number" inputMode="numeric" value={creditos} onChange={(e) => setCreditos(e.target.value)} erro={erros.numCreditos} required />
      </div>
      <div className={formulario.acoes}>
        <Botao type="submit" carregando={enviando}>
          {enviando ? 'Salvando…' : 'Salvar'}
        </Botao>
        <Botao onClick={aoConcluir}>Cancelar</Botao>
      </div>
    </form>
  )
}

interface FormDisciplinaProps {
  cursos: Curso[]
  disciplina?: Disciplina
  aoConcluir: () => void
}

function FormularioDisciplina({ cursos, disciplina, aoConcluir }: FormDisciplinaProps) {
  const { avisar } = useAviso()
  const [nome, setNome] = useState(disciplina?.nome ?? '')
  const [min, setMin] = useState(disciplina?.minAlunos ?? MINIMO_PADRAO)
  const [max, setMax] = useState(disciplina?.maxAlunos ?? MAXIMO_PADRAO)
  const [ativa, setAtiva] = useState(disciplina?.ativa ?? true)
  const [cursoIds, setCursoIds] = useState<number[]>(disciplina?.cursos.map((c) => c.id) ?? [])
  const [erros, setErros] = useState<Record<string, string>>({})
  const [enviando, setEnviando] = useState(false)

  const alternar = (id: number) => setCursoIds((atuais) => (atuais.includes(id) ? atuais.filter((x) => x !== id) : [...atuais, id]))

  async function salvar(e: FormEvent) {
    e.preventDefault()
    if (min > max) {
      setErros({ limites: 'O mínimo de alunos não pode passar do máximo.' })
      return
    }
    setEnviando(true)
    setErros({})
    try {
      const dados = { nome, minAlunos: min, maxAlunos: max, ativa, cursoIds }
      if (disciplina) await api.disciplinas.atualizar(disciplina.id, dados)
      else await api.disciplinas.criar(dados)
      avisar('ok', `Disciplina ${nome} salva.`)
      aoConcluir()
    } catch (erro) {
      const campos = camposDe(erro)
      if (Object.keys(campos).length > 0) setErros(campos)
      else avisar('erro', mensagemDe(erro))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className={formulario.formulario} onSubmit={(e) => void salvar(e)} noValidate aria-label={disciplina ? 'Editar disciplina' : 'Nova disciplina'}>
      <Campo rotulo="Nome da disciplina" value={nome} onChange={(e) => setNome(e.target.value)} erro={erros.nome} required />
      <div className={formulario.colunas}>
        <Regua rotulo="Mínimo de alunos" valor={min} min={1} max={20} marcas={[{ valor: MINIMO_PADRAO, rotulo: 'padrão 3' }]} aoMudar={setMin} />
        <Regua rotulo="Máximo de alunos" valor={max} min={1} max={100} marcas={[{ valor: MAXIMO_PADRAO, rotulo: 'padrão 60' }]} aoMudar={setMax} />
      </div>
      {erros.limites && (
        <p role="alert" className={formulario.subtexto}>
          ■ {erros.limites}
        </p>
      )}
      <fieldset className={formulario.lista} style={{ border: 0, padding: 0, margin: 0 }}>
        <legend className={formulario.subtexto}>Cursos</legend>
        {cursos.map((c) => (
          <label key={c.id} className={formulario.marcador}>
            <input type="checkbox" checked={cursoIds.includes(c.id)} onChange={() => alternar(c.id)} />
            {c.nome}
          </label>
        ))}
      </fieldset>
      <Opcoes
        legenda="Situação"
        nome="ativa"
        valor={ativa ? 'ativa' : 'inativa'}
        aoMudar={(v) => setAtiva(v === 'ativa')}
        opcoes={[
          { valor: 'ativa', rotulo: 'Ativa' },
          { valor: 'inativa', rotulo: 'Inativa' },
        ]}
      />
      <div className={formulario.acoes}>
        <Botao type="submit" carregando={enviando}>
          {enviando ? 'Salvando…' : 'Salvar'}
        </Botao>
        <Botao onClick={aoConcluir}>Cancelar</Botao>
      </div>
    </form>
  )
}

/** Catálogo que a secretaria mantém para montar os currículos: cursos e disciplinas (com seus limites de alunos). */
export function Disciplinas() {
  const [carga, recarregar] = useCarregar(async () => {
    const [cursos, disciplinas] = await Promise.all([api.cursos.listar(), api.disciplinas.listar()])
    return { cursos, disciplinas }
  })
  const [novoCurso, setNovoCurso] = useState(false)
  const [editando, setEditando] = useState<number | 'nova' | null>(null)

  const concluir = () => {
    setNovoCurso(false)
    setEditando(null)
    recarregar()
  }

  return (
    <Pagina kicker="Secretaria" titulo="Disciplinas">
      <Carga carga={carga} tentar={recarregar}>
        {({ cursos, disciplinas }) => (
          <>
            <Secao titulo="Disciplinas">
              <div className={formulario.acoes}>
                <Botao onClick={() => setEditando('nova')}>
                  <Icone de={Plus} size={18} /> Nova disciplina
                </Botao>
              </div>
              {editando === 'nova' && <FormularioDisciplina cursos={cursos} aoConcluir={concluir} />}
              {disciplinas.length === 0 ? (
                <p className={formulario.vazio}>Nenhuma disciplina ainda.</p>
              ) : (
                <Linhas rotulo="Disciplinas">
                  {disciplinas.map((d, i) => (
                    <Fragment key={d.id}>
                      <Linha indice={pad2(i + 1)} aoSelecionar={() => setEditando(editando === d.id ? null : d.id)} selecionada={editando === d.id}>
                        <strong role="cell">
                          {d.nome}
                          {!d.ativa && <Marca>inativa</Marca>}
                        </strong>
                        <span role="cell">{d.cursos.map((c) => c.nome).join(' · ') || 'Sem curso'}</span>
                        <span role="cell" data-fim className={formulario.fim}>
                          {d.minAlunos} a {d.maxAlunos} alunos
                        </span>
                      </Linha>
                      {editando === d.id && (
                        <Painel>
                          <FormularioDisciplina cursos={cursos} disciplina={d} aoConcluir={concluir} />
                        </Painel>
                      )}
                    </Fragment>
                  ))}
                </Linhas>
              )}
            </Secao>

            <Secao titulo="Cursos">
              <div className={formulario.acoes}>
                <Botao onClick={() => setNovoCurso(true)}>Novo curso</Botao>
              </div>
              {novoCurso && <FormularioCurso aoConcluir={concluir} />}
              <Linhas rotulo="Cursos">
                {cursos.map((c, i) => (
                  <Linha key={c.id} indice={pad2(i + 1)}>
                    <strong role="cell">{c.nome}</strong>
                    <span role="cell" data-fim className={formulario.fim}>
                      {c.numCreditos} créditos
                    </span>
                  </Linha>
                ))}
              </Linhas>
            </Secao>
          </>
        )}
      </Carga>
    </Pagina>
  )
}
