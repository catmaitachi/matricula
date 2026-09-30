# CLAUDE.md — Sistema de Matrículas

Trabalho de Laboratório de Desenvolvimento de Software (grupo de 4). Não vai para produção, **mas o padrão de qualidade e de segurança é o de um sistema real**: o grupo já recusou atalhos "de laboratório" (ex.: token em `sessionStorage`). Responder e escrever código, comentários e mensagens de erro em **português (pt-BR)**.

## Subir e testar

```bash
# backend (8080), perfil dev = contas e dados de exemplo
cd backend && mvn install && java -jar api/target/api-0.1.0.jar --spring.profiles.active=dev
# frontend (5173), o Vite encaminha /api para o backend
cd frontend && npm install && npm run dev
# testes
cd backend && mvn test                 # 93 testes (dominio 19, pagamento 5, api 69)
cd frontend && npm test                # 123 testes, inclui axe em todas as telas
cd frontend && npm run typecheck && npm run lint && npm run build
# ponta a ponta (26 passos) contra o jar, instância limpa
MATRICULA_BOOTSTRAP_SENHA=x-uma-senha-longa java -jar backend/api/target/api-0.1.0.jar --server.port=8081 --spring.datasource.url=jdbc:h2:mem:e2e &
E2E_SENHA_SECRETARIA=x-uma-senha-longa python3 scripts/e2e.py http://localhost:8081
```

Contas de teste do perfil `dev` (senhas públicas): ver a tabela em [`README.md`](README.md#contas-de-teste-perfil-dev). Para voltar aos dados de exemplo: parar o backend, apagar `backend/data/` e subir de novo. Fora do `dev` não há nenhuma conta nem senha padrão: a primeira secretaria vem de `MATRICULA_BOOTSTRAP_SENHA` (12+ caracteres).

## Onde está o quê

- `backend/dominio`: entidades JPA com as regras dentro delas (`Aluno.matricular`, `Curriculo.encerrar`). **Sem Spring.** Só a anotação `jakarta.persistence`.
- `backend/pagamento`: porta `SistemaPagamento` (idempotente por chave) + `PagamentoFake`. Não depende do domínio. Trocar por cliente HTTP real não toca no resto.
- `backend/api`: controllers finos → serviços → repositórios. Segurança em `seguranca/`. Toda saída passa por `servico/Visoes`, toda entrada por `servico/Comandos` (com Bean Validation).
- `frontend/src/api`: **único** ponto de contato com o backend. `componentes/` = peças de interação (cada uma com o próprio CSS) e `Icone.tsx` (lucide). `layout/` = trilho, cena da página, tema. `paginas/{aluno,professor,secretaria}` e `paginas/entrar` (o login é autônomo: não usa nada global).
- `.design/`: decisões do `/inspiration` (`conceito.json`, `paginas.json` com páginas e componentes escolhidos). **Não há design system global nem tokens**: a paleta mínima está em `frontend/src/estilos/base.css` (`--texto`, `--fundo`, `--destaque`, `--erro`...; `--fio` = espessura dos traços). Nada de cor literal fora de `base.css` e do login.
- `docs/`: `requisitos.md`, `api.md`, `estrategia-de-testes.md`, `adr/0001..0003`.

## Regras que não se negociam

**Segurança** (detalhes e limites em `docs/adr/0003-seguranca.md`):
- Sessão = cookie `HttpOnly; Secure; SameSite=Strict`. **Nada de sessão/token em `localStorage`/`sessionStorage`** (a única chave é `matricula.tema`, preferência visual). Escritas levam `X-XSRF-TOKEN` (já centralizado em `src/api/http.ts`).
- Senha só como hash bcrypt (`{bcrypt}`), 8–72 bytes. Nunca devolver hash/senha em resposta nem log.
- Autorização por papel **e por objeto**: o id do aluno/professor vem da sessão, nunca do pedido; recurso alheio responde 404.
- Erro sempre `{codigo, mensagem}` sem pilha/SQL. Toda rota nova precisa de teste de 401/403 e de acesso a objeto alheio.
- Nunca ligar `matricula.seed.habilitado` fora de desenvolvimento.

**Domínio:** limite por semestre = até **4 obrigatórias e 2 optativas**; turma tem mínimo 3 e máximo 60 (vêm da disciplina). Vaga e limite do aluno são protegidos por trava pessimista (aluno, depois turma; nessa ordem). Chamada ao sistema de pagamento **fora** da transação do banco. Cancelar matrícula/turma cancela a cobrança (`cancelarCobranca`); falha do pagamento ao matricular = matrícula desfeita (503).

**Design (preferências do grupo):** o login é a âncora visual e não muda. Papel granulado, cor de destaque, traços finos (1 a 1,5px), cena por tela com título gigante. **Não alternar grandes superfícies claras e escuras na mesma tela** (inversão só em estado pequeno). Estado sempre **discreto e em texto primeiro**. Item indisponível = cor atenuada + etiqueta escrita, **sem tachado nem tracejado**. Ação sem volta = botão de segurar (`BotaoSegurar`). Ícones só de `lucide-react` via `Icone`, decorativos, com texto ao lado. Tema automático/claro/escuro (botão no trilho). No celular o trilho é uma gaveta aberta por menu hambúrguer (nada de barra de itens no topo). **Espaço e alinhamento:** usar a escala `--e1..--e6` (múltiplos de 8), `--margem` (margem da página) e `--medida` (largura de leitura: cena e conteúdo terminam na mesma borda). Alinhar índice, estado e contador pelo topo da linha; o conteúdo de um painel começa na coluna do título da linha; no celular o contador desce para a coluna do título. Respeitar `prefers-reduced-motion`; alvo de toque ≥ 44px; contraste AA.

**Qualidade:** toda regra nova tem teste; teste de regra crítica deve falhar se a regra for quebrada (já fizemos mutação manual nas fronteiras). Rodar `mvn test`, `npm test`, `typecheck`, `lint` antes de dizer que terminou.

## Armadilhas já pisadas (não repetir)

- `csrf()` do `spring-security-test` troca o repositório de token do filtro **de forma permanente** e esconde o cookie real: teste de cookie/CSRF vai em `SegurancaHttpTest` (servidor real, porta aleatória).
- `application.yml` em `src/test/resources` **substitui** o principal; usar `application.properties` (o Maven também não apaga recursos removidos de `target/test-classes`).
- `saveAndFlush` numa entidade já gerenciada faz merge e devolve **cópia**: para o cascade gravar o filho novo basta `flush()`.
- Campo de proxy Hibernate é nulo: comparar entidades pelo **método** (`outra.id()`), não pelo campo. Só aparece com banco (testes de domínio sem banco não veem).
- `document.cookie` só enxerga `XSRF-TOKEN`; a sessão é invisível ao JS (é o objetivo).
- `pgrep -f`/`pkill -f` com padrão solto casa com o próprio shell e o derruba (exit 144): ancorar (`'^java -jar api/target'`).
- Teste de front (jsdom) não vê CSS: depois de mexer em estilo, conferir no navegador (backend com `--spring.datasource.url=jdbc:h2:mem:visual`). `pkill -f 'vite'` solto também derruba o shell: matar por porta (`ss -ltnp`).
- A porta 5173 já foi usada por outro projeto do usuário (há `localStorage` `portfolio.medicao` na origem; não é nosso).
- Testes de `BotaoSegurar`/toast usam relógio falso (`testes/segurar.ts`); `findByRole('alert')` acha também a região permanente do toast: restringir com `within(...)`.

## Estado e pendências

- Sistema completo e verde. **Nada foi commitado** (`.design/`, `backend/`, `docs/`, `frontend/`, `scripts/` untracked; remoção de `java/` só staged). O design foi refeito em 2026-09-29 a pedido do usuário (tudo menos o login). O `java/` original segue no histórico.
- Limites conhecidos (ADR-0003): bloqueio de login e sessões em memória (1 instância); trocar senha não derruba sessões abertas; o servidor que entregar o front em produção precisa do próprio CSP; sem migrações de esquema (`ddl-auto=update`, usaria Flyway); sem recuperação de senha nem auditoria.
- O grafo de conhecimento do `/understand` está em `.ua/` (ignorado via `.git/info/exclude`).
