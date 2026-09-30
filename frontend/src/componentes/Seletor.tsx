import { useId, type Ref, type SelectHTMLAttributes } from 'react'
import { ChevronDown } from 'lucide-react'
import { Icone } from './Icone'
import estilos from './Campo.module.css'

interface Props extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'id'> {
  rotulo: string
  erro?: string
  ref?: Ref<HTMLSelectElement>
}

/** Lista suspensa no mesmo padrão do Campo (rótulo em mono, linha inferior que enche no foco). */
export function Seletor({ rotulo, erro, ref, children, ...selecao }: Props) {
  const id = useId()
  const idMensagem = `${id}-mensagem`

  return (
    <div className={estilos.campo} data-erro={erro ? '' : undefined}>
      <label htmlFor={id} className={estilos.rotulo}>
        {rotulo}
      </label>
      <div className={estilos.entrada}>
        <select {...selecao} ref={ref} id={id} aria-invalid={erro ? true : undefined} aria-describedby={erro ? idMensagem : undefined}>
          {children}
        </select>
        <Icone de={ChevronDown} size={20} className={estilos.seta} />
        <span className={estilos.linha} aria-hidden="true" />
      </div>
      {erro && (
        <p id={idMensagem} className={estilos.mensagem} role="alert">
          {erro}
        </p>
      )}
    </div>
  )
}
