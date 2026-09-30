import { useId, type InputHTMLAttributes, type Ref } from 'react'
import estilos from './Campo.module.css'

interface Props extends Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> {
  rotulo: string
  erro?: string
  ref?: Ref<HTMLInputElement>
}

/** Campo de texto: linha inferior que enche no foco; o erro enche a linha e aparece como alerta. */
export function Campo({ rotulo, erro, ref, ...entrada }: Props) {
  const id = useId()
  const idMensagem = `${id}-mensagem`

  return (
    <div className={estilos.campo} data-erro={erro ? '' : undefined}>
      <label htmlFor={id} className={estilos.rotulo}>
        {rotulo}
      </label>
      <div className={estilos.entrada}>
        <input
          {...entrada}
          ref={ref}
          id={id}
          aria-invalid={erro ? true : undefined}
          aria-describedby={erro ? idMensagem : undefined}
        />
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
