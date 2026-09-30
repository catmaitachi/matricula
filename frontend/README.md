# Frontend — Sistema de Matrículas

React 19 + TypeScript + Vite. Estilo com CSS Modules e uma paleta mínima em `src/estilos/base.css` (papel, tinta, destaque; tema automático, claro ou escuro pelo botão do menu). As decisões de design estão em [`../.design`](../.design). Ícones: `lucide-react`, via `src/componentes/Icone.tsx`.

## Rodar

O backend precisa estar em `http://localhost:8080` (ver o [README principal](../README.md)). O Vite encaminha `/api` para ele, então navegador e API ficam na mesma origem (cookies sem CORS).

```bash
npm install
npm run dev        # http://localhost:5173
```

## Qualidade

```bash
npm test           # Vitest + Testing Library + axe (acessibilidade de cada tela)
npm run typecheck  # tsc estrito
npm run lint       # oxlint
npm run build
```

## Estrutura

- `src/api/`: o único ponto de contato com o backend (`index.ts`), cliente HTTP com CSRF e tratamento de sessão expirada (`http.ts`), tipos e rótulos.
- `src/auth/`: quem está logado (em memória; a sessão é cookie `HttpOnly`), guardas de rota por sessão e por papel.
- `src/componentes/`: as peças de interação (Campo, Botao, BotaoSegurar, Linhas, Registro, Opcoes, Regua, Aviso, Icone...), cada uma com o próprio CSS.
- `src/layout/`: casca com o trilho lateral (ícones, tema, cortina entre telas; no celular vira gaveta aberta por um menu hambúrguer) e a cena de cada página.
- `src/paginas/`: telas por papel (`aluno/`, `professor/`, `secretaria/`) e a entrada.
