import { Navigate, Route, Routes } from 'react-router-dom'
import { RequerPapel } from './auth/RequerPapel'
import { RequerSessao } from './auth/RequerSessao'
import { useAuth } from './auth/contexto'
import { Casca } from './layout/Casca'
import { NAVEGACAO } from './layout/navegacao'
import { Entrar } from './paginas/entrar/Entrar'
import { CurriculoDoAluno } from './paginas/aluno/CurriculoDoAluno'
import { MinhasMatriculas } from './paginas/aluno/MinhasMatriculas'
import { AlunosDaTurma } from './paginas/professor/AlunosDaTurma'
import { TurmasDoProfessor } from './paginas/professor/TurmasDoProfessor'
import { Contas } from './paginas/secretaria/Contas'
import { Curriculo } from './paginas/secretaria/Curriculo'
import { Curriculos } from './paginas/secretaria/Curriculos'
import { Disciplinas } from './paginas/secretaria/Disciplinas'

/** A raiz leva cada papel à sua primeira tela. */
function Inicio() {
  const { usuario } = useAuth()
  return usuario ? <Navigate to={NAVEGACAO[usuario.papel][0]!.para} replace /> : null
}

export function Rotas() {
  return (
    <Routes>
      <Route path="/entrar" element={<Entrar />} />
      <Route
        element={
          <RequerSessao>
            <Casca />
          </RequerSessao>
        }
      >
        <Route index element={<Inicio />} />
        <Route path="aluno" element={<RequerPapel papel="ALUNO" />}>
          <Route path="curriculo" element={<CurriculoDoAluno />} />
          <Route path="matriculas" element={<MinhasMatriculas />} />
        </Route>
        <Route path="professor" element={<RequerPapel papel="PROFESSOR" />}>
          <Route path="turmas" element={<TurmasDoProfessor />} />
          <Route path="turmas/:id" element={<AlunosDaTurma />} />
        </Route>
        <Route path="secretaria" element={<RequerPapel papel="SECRETARIA" />}>
          <Route path="contas" element={<Contas />} />
          <Route path="disciplinas" element={<Disciplinas />} />
          <Route path="curriculos" element={<Curriculos />} />
          <Route path="curriculos/:id" element={<Curriculo />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
