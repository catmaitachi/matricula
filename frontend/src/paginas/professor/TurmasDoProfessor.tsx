import { useNavigate } from 'react-router-dom'
import { api } from '../../api'
import { ROTULO_ESTADO_TURMA, ROTULO_TURNO } from '../../api/rotulos'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas, Marca } from '../../componentes/Linhas'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina } from '../../layout/Pagina'
import { pad2 } from '../../util'

/** RF03: as turmas do professor; escolher uma mostra os alunos matriculados. */
export function TurmasDoProfessor() {
  const [carga, recarregar] = useCarregar(() => api.professor.turmas())
  const navegar = useNavigate()

  return (
    <Pagina kicker="Professor" titulo="Minhas turmas">
      <Carga carga={carga} tentar={recarregar}>
        {(turmas) =>
          turmas.length === 0 ? (
            <p className={formulario.vazio}>Você ainda não tem turmas em semestres abertos. Quando a secretaria montar o currículo, elas aparecem aqui.</p>
          ) : (
            <Linhas rotulo="Turmas">
              {turmas.map((t, i) => (
                <Linha key={t.id} indice={pad2(i + 1)} aoSelecionar={() => navegar(`/professor/turmas/${t.id}`)}>
                  <strong role="cell">
                    {t.disciplina}
                    {t.estado !== 'ABERTA' && <Marca>{ROTULO_ESTADO_TURMA[t.estado]}</Marca>}
                  </strong>
                  <span role="cell">
                    Turma {t.codigo} · {ROTULO_TURNO[t.turno]} · {t.semestre}
                  </span>
                  <span role="cell" data-fim className={formulario.fim}>
                    {t.matriculados}/{t.maxAlunos} alunos
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
