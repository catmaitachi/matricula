import { BookOpen, CalendarRange, ClipboardList, Library, Presentation, Users, type LucideIcon } from 'lucide-react'
import type { Papel } from '../api/tipos'

interface Item {
  para: string
  rotulo: string
  icone: LucideIcon
}

/** O menu de cada papel; o primeiro item também é a tela inicial de quem entra. */
export const NAVEGACAO: Record<Papel, readonly Item[]> = {
  ALUNO: [
    { para: '/aluno/curriculo', rotulo: 'Currículo', icone: BookOpen },
    { para: '/aluno/matriculas', rotulo: 'Minhas matrículas', icone: ClipboardList },
  ],
  PROFESSOR: [{ para: '/professor/turmas', rotulo: 'Minhas turmas', icone: Presentation }],
  SECRETARIA: [
    { para: '/secretaria/contas', rotulo: 'Contas', icone: Users },
    { para: '/secretaria/disciplinas', rotulo: 'Disciplinas', icone: Library },
    { para: '/secretaria/curriculos', rotulo: 'Currículos', icone: CalendarRange },
  ],
}
