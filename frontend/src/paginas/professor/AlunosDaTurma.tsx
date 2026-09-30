import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../../api'
import { ROTULO_TIPO, ROTULO_TURNO } from '../../api/rotulos'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas } from '../../componentes/Linhas'
import { Icone } from '../../componentes/Icone'
import { Registro } from '../../componentes/Registro'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina } from '../../layout/Pagina'
import { pad2 } from '../../util'

/** RF03: a lista de chamada de uma turma. Turma de outro professor responde como se não existisse. */
export function AlunosDaTurma() {
  const id = Number(useParams().id)
  const [carga, recarregar] = useCarregar(
    async () => {
      const [turmas, alunos] = await Promise.all([api.professor.turmas(), api.professor.alunos(id)])
      return { turma: turmas.find((t) => t.id === id), alunos }
    },
    [id],
  )

  return (
    <Carga carga={carga} tentar={recarregar}>
      {({ turma, alunos }) => (
        <Pagina
          kicker={turma ? `Turma ${turma.codigo} · ${ROTULO_TURNO[turma.turno]} · ${turma.semestre}` : 'Turma'}
          titulo={turma?.disciplina ?? 'Alunos'}
          acoes={<Link to="/professor/turmas" className={formulario.voltar}><Icone de={ArrowLeft} size={16} /> Minhas turmas</Link>}
          registros={
            turma && (
              <>
                <Registro rotulo="Matriculados">
                  {turma.matriculados}/{turma.maxAlunos}
                </Registro>
                <Registro rotulo="Mínimo para abrir">{turma.minAlunos}</Registro>
              </>
            )
          }
        >
          {alunos.length === 0 ? (
            <p className={formulario.vazio}>Nenhum aluno matriculado nesta turma ainda.</p>
          ) : (
            <Linhas rotulo="Alunos matriculados">
              {alunos.map((a, i) => (
                <Linha key={a.numPessoa} indice={pad2(i + 1)}>
                  <strong role="cell">{a.nome}</strong>
                  <span role="cell">
                    Matrícula {a.numMatricula} · {a.numPessoa}
                  </span>
                  <span role="cell" data-fim className={formulario.fim}>
                    {ROTULO_TIPO[a.tipo]}
                  </span>
                </Linha>
              ))}
            </Linhas>
          )}
        </Pagina>
      )}
    </Carga>
  )
}
