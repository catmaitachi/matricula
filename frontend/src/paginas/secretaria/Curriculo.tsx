import { useState, type FormEvent } from 'react'
import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../../api'
import { ROTULO_ESTADO_CURRICULO, ROTULO_ESTADO_TURMA, ROTULO_TURNO } from '../../api/rotulos'
import type { Turno } from '../../api/tipos'
import { Botao } from '../../componentes/Botao'
import { Icone } from '../../componentes/Icone'
import { BotaoSegurar } from '../../componentes/BotaoSegurar'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas, Marca } from '../../componentes/Linhas'
import { Registro } from '../../componentes/Registro'
import { Seletor } from '../../componentes/Seletor'
import { useAviso } from '../../componentes/contextoAviso'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina, Secao } from '../../layout/Pagina'
import { mensagemDe, pad2 } from '../../util'

/** RF01: monta as turmas do semestre, abre as inscrições e, no fim, encerra (a turma sem quórum cai). */
export function Curriculo() {
  const id = Number(useParams().id)
  const { avisar } = useAviso()
  const [carga, recarregar] = useCarregar(
    async () => {
      const [curriculo, disciplinas, professores] = await Promise.all([
        api.curriculos.detalhe(id),
        api.disciplinas.listar(),
        api.contas.listar('PROFESSOR'),
      ])
      return { curriculo, disciplinas: disciplinas.filter((d) => d.ativa), professores: professores.filter((p) => p.ativo) }
    },
    [id],
  )
  const [disciplinaId, setDisciplinaId] = useState('')
  const [professorId, setProfessorId] = useState('')
  const [turno, setTurno] = useState<Turno>('MANHA')
  const [enviando, setEnviando] = useState(false)

  /** Executa a ação, avisa o resultado (texto fixo ou montado a partir da resposta) e recarrega a tela. */
  async function agir<R>(acao: () => Promise<R>, sucesso: string | ((resposta: R) => string)) {
    setEnviando(true)
    try {
      const resposta = await acao()
      avisar('ok', typeof sucesso === 'function' ? sucesso(resposta) : sucesso)
    } catch (erro) {
      avisar('erro', mensagemDe(erro))
    } finally {
      setEnviando(false)
      recarregar()
    }
  }

  const adicionar = (e: FormEvent) => {
    e.preventDefault()
    void agir(() => api.curriculos.adicionarTurma(id, { disciplinaId: Number(disciplinaId), professorId: Number(professorId), turno }), 'Turma adicionada.')
  }

  return (
    <Carga carga={carga} tentar={recarregar}>
      {({ curriculo, disciplinas, professores }) => {
        const rascunho = curriculo.estado === 'RASCUNHO'
        return (
          <Pagina
            kicker={`Semestre · ${ROTULO_ESTADO_CURRICULO[curriculo.estado]}`}
            titulo={curriculo.semestre}
            acoes={<Link to="/secretaria/curriculos" className={formulario.voltar}><Icone de={ArrowLeft} size={16} /> Currículos</Link>}
            registros={
              <>
                <Registro rotulo="Turmas">{curriculo.turmas.length}</Registro>
                <Registro rotulo="Confirmadas">{curriculo.turmas.filter((t) => t.estado === 'CONFIRMADA').length}</Registro>
                <Registro rotulo="Canceladas">{curriculo.turmas.filter((t) => t.estado === 'CANCELADA').length}</Registro>
              </>
            }
          >
            {rascunho && (
              <Secao titulo="Adicionar turma">
                <form className={formulario.formulario} onSubmit={adicionar} aria-label="Adicionar turma">
                  <div className={formulario.colunas}>
                    <Seletor rotulo="Disciplina" value={disciplinaId} onChange={(e) => setDisciplinaId(e.target.value)} required>
                      <option value="">Escolha…</option>
                      {disciplinas.map((d) => (
                        <option key={d.id} value={d.id}>
                          {d.nome}
                        </option>
                      ))}
                    </Seletor>
                    <Seletor rotulo="Professor" value={professorId} onChange={(e) => setProfessorId(e.target.value)} required>
                      <option value="">Escolha…</option>
                      {professores.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.nome}
                        </option>
                      ))}
                    </Seletor>
                    <Seletor rotulo="Turno" value={turno} onChange={(e) => setTurno(e.target.value as Turno)}>
                      {(Object.keys(ROTULO_TURNO) as Turno[]).map((t) => (
                        <option key={t} value={t}>
                          {ROTULO_TURNO[t]}
                        </option>
                      ))}
                    </Seletor>
                  </div>
                  <div className={formulario.acoes}>
                    <Botao type="submit" carregando={enviando} disabled={!disciplinaId || !professorId}>
                      Adicionar
                    </Botao>
                  </div>
                </form>
              </Secao>
            )}

            <Secao titulo="Turmas">
              {curriculo.turmas.length === 0 ? (
                <p className={formulario.vazio}>Nenhuma turma ainda. Adicione turmas antes de abrir as inscrições.</p>
              ) : (
                <Linhas rotulo="Turmas do semestre">
                  {curriculo.turmas.map((t, i) => (
                    <Linha key={t.id} indice={pad2(i + 1)}>
                      <strong role="cell">
                        {t.disciplina}
                        {t.estado !== 'ABERTA' && <Marca>{ROTULO_ESTADO_TURMA[t.estado]}</Marca>}
                      </strong>
                      <span role="cell">
                        Turma {t.codigo} · {ROTULO_TURNO[t.turno]} · {t.professor}
                      </span>
                      <span role="cell" data-fim className={formulario.fim}>
                        {t.ocupadas}/{t.maxAlunos} (mín. {t.minAlunos})
                      </span>
                      {rascunho && (
                        <div role="cell">
                          <Botao
                            onClick={() => void agir(() => api.curriculos.removerTurma(id, t.id), `Turma ${t.codigo} de ${t.disciplina} removida.`)}
                            disabled={enviando}
                          >
                            Remover
                          </Botao>
                        </div>
                      )}
                    </Linha>
                  ))}
                </Linhas>
              )}
            </Secao>

            {rascunho && (
              <div className={formulario.acoes}>
                <Botao
                  onClick={() => void agir(() => api.curriculos.abrir(id), `Inscrições de ${curriculo.semestre} abertas.`)}
                  carregando={enviando}
                  disabled={curriculo.turmas.length === 0}
                >
                  Abrir inscrições
                </Botao>
              </div>
            )}
            {curriculo.estado === 'ABERTO' && (
              <Secao titulo="Encerrar inscrições">
                <p className={formulario.subtexto}>
                  Turma com menos alunos que o mínimo é cancelada, e as cobranças dos alunos dela são canceladas. Depois disso, ninguém mais se matricula nem cancela.
                </p>
                <BotaoSegurar
                  duracao={1500}
                  concluido="Encerrado"
                  dica="Segure por 1,5 segundo para encerrar (mouse, toque ou Espaço)."
                  aoConfirmar={() =>
                    agir(
                      () => api.curriculos.encerrar(id),
                      (r) => `Inscrições encerradas: ${r.turmasCanceladas} turma(s) e ${r.matriculasCanceladas} matrícula(s) canceladas por falta de quórum.`,
                    )
                  }
                >
                  Segure para encerrar
                </BotaoSegurar>
              </Secao>
            )}
          </Pagina>
        )
      }}
    </Carga>
  )
}
