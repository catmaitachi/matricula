import { Fragment, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../../api'
import { ROTULO_COBRANCA, ROTULO_TIPO, ROTULO_TURNO } from '../../api/rotulos'
import { ErroApi, type TipoMatricula, type TurmaParaAluno } from '../../api/tipos'
import { Botao } from '../../componentes/Botao'
import { Carga } from '../../componentes/Carga'
import { Caixinhas, Registro } from '../../componentes/Registro'
import { Linha, Linhas, Marca, Painel } from '../../componentes/Linhas'
import { Opcoes } from '../../componentes/Opcoes'
import { useAviso } from '../../componentes/contextoAviso'
import { useCarregar } from '../../componentes/useCarregar'
import { Pagina } from '../../layout/Pagina'
import formulario from '../../estilos/formulario.module.css'
import { mensagemDe, pad2 } from '../../util'

const semCurriculoAberto = (erro: unknown) => {
  if (erro instanceof ErroApi && erro.codigo === 'SEM_CURRICULO_ABERTO') return null
  throw erro
}

/** RF02: o aluno vê as turmas do semestre aberto e se matricula, escolhendo se é obrigatória ou optativa. */
export function CurriculoDoAluno() {
  const { avisar } = useAviso()
  const [carga, recarregar] = useCarregar(async () => {
    const [curriculo, minhas] = await Promise.all([api.aluno.curriculo().catch(semCurriculoAberto), api.aluno.matriculas()])
    return { curriculo, minhas }
  })
  const [escolhida, setEscolhida] = useState<number | null>(null)
  const [tipo, setTipo] = useState<TipoMatricula>('OBRIGATORIA')
  const [enviando, setEnviando] = useState(false)

  async function matricular(turma: TurmaParaAluno) {
    setEnviando(true)
    try {
      const matricula = await api.aluno.matricular({ turmaId: turma.id, tipo })
      avisar('ok', `Matriculado em ${matricula.disciplina}. Cobrança ${ROTULO_COBRANCA[matricula.cobranca]}.`)
      setEscolhida(null)
    } catch (erro) {
      avisar('erro', mensagemDe(erro))
    } finally {
      setEnviando(false)
      recarregar() // vagas e limites mudam a cada tentativa
    }
  }

  return (
    <Carga carga={carga} tentar={recarregar}>
      {({ curriculo, minhas }) => {
        if (!curriculo) {
          return (
            <Pagina kicker="Inscrições" titulo="Currículo">
              <p className={formulario.vazio}>Não há inscrições abertas no momento. Quando a secretaria abrir o semestre, as turmas aparecem aqui.</p>
            </Pagina>
          )
        }
        const cheio = (t: TipoMatricula) => (t === 'OBRIGATORIA' ? minhas.obrigatorias >= minhas.limiteObrigatorias : minhas.optativas >= minhas.limiteOptativas)
        return (
          <Pagina
            kicker={`Semestre ${curriculo.semestre}`}
            titulo="Currículo"
            registros={
              <>
                <Registro rotulo="Obrigatórias" alerta={cheio('OBRIGATORIA')}>
                  {minhas.obrigatorias}/{minhas.limiteObrigatorias}
                  <Caixinhas usadas={minhas.obrigatorias} total={minhas.limiteObrigatorias} />
                </Registro>
                <Registro rotulo="Optativas" alerta={cheio('OPTATIVA')}>
                  {minhas.optativas}/{minhas.limiteOptativas}
                  <Caixinhas usadas={minhas.optativas} total={minhas.limiteOptativas} />
                </Registro>
              </>
            }
          >
            <Linhas rotulo="Turmas do semestre">
              {curriculo.turmas.map((t, i) => {
                const lotada = t.ocupadas >= t.maxAlunos
                const minha = t.minhaMatriculaId !== null
                const aberta = escolhida === t.id
                return (
                  <Fragment key={t.id}>
                    <Linha
                      indice={pad2(i + 1)}
                      aoSelecionar={() => setEscolhida(aberta ? null : t.id)}
                      selecionada={aberta}
                      indisponivel={lotada && !minha}
                      detalhe={`${Math.max(0, t.maxAlunos - t.ocupadas)} vagas livres · mínimo de ${t.minAlunos} para abrir`}
                    >
                      <strong role="cell">
                        {t.disciplina}
                        {minha && <Marca>matriculado</Marca>}
                        {lotada && !minha && <Marca>lotada</Marca>}
                        {!lotada && t.ocupadas < t.minAlunos && <Marca>faltam {t.minAlunos - t.ocupadas} para abrir</Marca>}
                      </strong>
                      <span role="cell">
                        Turma {t.codigo} · {ROTULO_TURNO[t.turno]} · {t.professor}
                      </span>
                      <span role="cell" data-fim className={formulario.fim}>
                        {t.ocupadas}/{t.maxAlunos}
                      </span>
                    </Linha>
                    {aberta && (
                      <Painel>
                        {minha ? (
                          <p>
                            Você já está nesta turma. Para desfazer, vá em <Link to="/aluno/matriculas">Minhas matrículas</Link>.
                          </p>
                        ) : (
                          <>
                            <Opcoes
                              legenda="Tipo da matrícula"
                              nome={`tipo-${t.id}`}
                              valor={tipo}
                              aoMudar={setTipo}
                              opcoes={(['OBRIGATORIA', 'OPTATIVA'] as const).map((v) => ({ valor: v, rotulo: ROTULO_TIPO[v] }))}
                            />
                            {cheio(tipo) && (
                              <p className={formulario.subtexto}>
                                Você já tem o máximo de {tipo === 'OBRIGATORIA' ? 'obrigatórias' : 'optativas'}. Cancele uma para continuar.
                              </p>
                            )}
                            <div className={formulario.acoes}>
                              <Botao onClick={() => void matricular(t)} carregando={enviando} disabled={cheio(tipo)}>
                                {enviando ? 'Matriculando…' : 'Matricular'}
                              </Botao>
                            </div>
                          </>
                        )}
                      </Painel>
                    )}
                  </Fragment>
                )
              })}
            </Linhas>
          </Pagina>
        )
      }}
    </Carga>
  )
}
