import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Rotas } from '../Rotas'
import { AuthProvider } from '../auth/AuthProvider'
import type { Usuario } from '../api/tipos'
import { ErroApi } from '../api/tipos'
import { AvisoProvider } from '../componentes/AvisoProvider'
import { apiMock } from './apiMock'
import { SESSAO_ALUNO } from './fixtures'

/** Monta o app inteiro numa rota, já entrado como `usuario` (ou visitante, com `null`). */
export function renderizar(rota: string, usuario: Usuario | null = SESSAO_ALUNO.usuario) {
  if (usuario) apiMock.eu.mockResolvedValue(usuario)
  else apiMock.eu.mockRejectedValue(new ErroApi(401, 'NAO_AUTENTICADO', 'sem sessão'))
  return render(
    <MemoryRouter initialEntries={[rota]}>
      <AuthProvider>
        <AvisoProvider>
          <Rotas />
        </AvisoProvider>
      </AuthProvider>
    </MemoryRouter>,
  )
}
