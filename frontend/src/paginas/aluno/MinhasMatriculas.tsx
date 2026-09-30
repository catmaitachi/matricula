import { Link } from 'react-router-dom'
import { api } from '../../api'
import { ROTULO_ESTADO_CURRICULO, ROTULO_TIPO, ROTULO_TURNO } from '../../api/rotulos'
import type { Matricula } from '../../api/tipos'
import { BotaoSegurar } from '../../componentes/BotaoSegurar'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas, Marca } from '../../componentes/Linhas'
import { Caixinhas, Cobranca, Registro } from '../../componentes/Registro'
import { useAviso } from '../../componentes/contextoAviso'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina } from '../../layout/Pagina'
import { mensagemDe, pad2 } from '../../util'

/** RF02 e RF05: as matrículas do semestre, a situação da cobrança de cada uma e o cancelamento. */
export function MinhasMatriculas() {
  const { avisar } = useAviso()
  const [carga, recarregar] = useCarregar(() => api.aluno.matriculas())

  async function cancelar(matricula: Matricula) {
    try {
      await api.aluno.cancelar(matricula.id)
      avisar('ok', `Matrícula em ${matricula.disciplina} cancelada e cobrança cancelada.`)
    } catch (erro) {
      avisar('erro', mensagemDe(erro))
    } finally {
      recarregar()
    }
  }

  return (
    <Carga carga={carga} tentar={recarregar}>
      {(m) => (
        <Pagina
          kicker={m.semestre && m.estado ? `Semestre ${m.semestre} · ${ROTULO_ESTADO_CURRICULO[m.estado]}` : 'Sem semestre'}
          titulo="Minhas matrículas"
          registros={
            <>
              <Registro rotulo="Obrigatórias" alerta={m.obrigatorias >= m.limiteObrigatorias}>
                {m.obrigatorias}/{m.limiteObrigatorias}
                <Caixinhas usadas={m.obrigatorias} total={m.limiteObrigatorias} />
              </Registro>
              <Registro rotulo="Optativas" alerta={m.optativas >= m.limiteOptativas}>
                {m.optativas}/{m.limiteOptativas}
                <Caixinhas usadas={m.optativas} total={m.limiteOptativas} />
              </Registro>
            </>
          }
        >
          {m.matriculas.length === 0 ? (
            <p className={formulario.vazio}>
              Você ainda não tem matrículas neste semestre. <Link to="/aluno/curriculo">Ver o currículo</Link>.
            </p>
          ) : (
            <Linhas rotulo="Minhas matrículas">
              {m.matriculas.map((x, i) => {
                const caiu = x.estado === 'CANCELADA_SEM_QUORUM'
                return (
                  <Linha key={x.id} indice={pad2(i + 1)} indisponivel={caiu}>
                    <strong role="cell">
                      {x.disciplina}
                      {caiu && <Marca>cancelada: turma sem quórum</Marca>}
                    </strong>
                    <span role="cell">
                      Turma {x.codigo} · {ROTULO_TURNO[x.turno]} · {ROTULO_TIPO[x.tipo]}
                    </span>
                    <span role="cell" data-fim>
                      <Cobranca status={x.cobranca} />
                    </span>
                    {!caiu && m.estado === 'ABERTO' && (
                      <div role="cell">
                        <BotaoSegurar
                          concluido="Cancelada"
                          dica="Segure por 1 segundo para cancelar (mouse, toque ou Espaço)."
                          aoConfirmar={() => cancelar(x)}
                        >
                          Segure para cancelar
                        </BotaoSegurar>
                      </div>
                    )}
                  </Linha>
                )
              })}
            </Linhas>
          )}
        </Pagina>
      )}
    </Carga>
  )
}
