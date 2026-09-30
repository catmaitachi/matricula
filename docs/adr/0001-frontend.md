# ADR-0001: Stack do frontend

**Status:** Aceito
**Data:** 2026-09-29
**Decisores:** grupo do laboratório

## Contexto
O sistema de matrículas precisa de uma interface para três papéis (aluno, professor, secretaria).
É um trabalho de laboratório: não vai para produção, mas o código deve ser testável e legível.
O design (tipografia gigante, papel granulado, cor de destaque, cena por tela) foi definido nas vitrines do `/inspiration`, sem design system global: cada peça tem o próprio CSS Module e só divide a paleta mínima de `src/estilos/base.css`.

## Decisão
React 19 + TypeScript estrito + Vite; roteamento com `react-router-dom`; estilos com CSS Modules
(paleta mínima em variáveis CSS, tema automático/claro/escuro); ícones com `lucide-react`; testes com Vitest e Testing Library; lint com oxlint.
Toda chamada ao backend passa por um único módulo (`src/api`), com tipos que espelham as respostas. A sessão é um cookie `HttpOnly` gerenciado pelo navegador: o app não guarda token nem nada de sessão em `localStorage`/`sessionStorage` (ver ADR-0003). A única chave em `localStorage` é `matricula.tema`, uma preferência visual (automático, claro ou escuro), aplicada por `public/tema.js` antes da primeira pintura.
Ícones: `lucide-react`, que entra no bundle só com os usados; o envelope `componentes/Icone.tsx` fixa o traço reto e os marca como decorativos (sempre há texto ao lado).

## Opções consideradas
| Opção | Prós | Contras |
| :-- | :-- | :-- |
| **React + Vite + CSS Modules** (escolhida) | pedido pelo grupo; poucas dependências; o visual próprio entra sem adaptação | roteamento e formulários à mão |
| Tailwind + biblioteca de componentes | rápido para telas padrão | briga com o design próprio; mais dependências |
| Next.js | roteamento e SSR prontos | SSR sem necessidade; mais complexo para um SPA que fala com uma API |

## Consequências
- Fácil: trocar a paleta em `base.css` sem tocar nos componentes; testar telas por papel e acessibilidade.
- Difícil: sem biblioteca de formulários, cada tela valida à mão (aceitável para poucas telas).
- Difícil: cada chamada de escrita precisa devolver o token CSRF; isso fica centralizado em `src/api/http.ts`.
- Revisitar: se as telas crescerem muito, avaliar uma biblioteca de dados (TanStack Query) no lugar do hook `useCarregar`.
