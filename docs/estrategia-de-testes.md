# Estratégia de testes

Pirâmide: muitos testes de unidade rápidos nas regras, alguns de integração nas bordas (HTTP e banco),
poucos de ponta a ponta no fluxo principal. Cada etapa do fluxo do usuário só fecha com seus testes passando.

| Área | Tipo | O que cobre | Meta |
| :-- | :-- | :-- | :-- |
| Regras de domínio (backend) | unidade (JUnit, sem Spring) | limite de 6 matrículas, turma 3–60, duplicidade, tipo obrigatória/optativa, quórum | 100% das regras de negócio, incluindo bordas (5→6, 59→60, 2→3) |
| Casos de uso (backend) | unidade com repositórios em memória | matricular, cancelar, encerrar inscrições, notificar pagamento | caminho feliz + cada erro de regra |
| API HTTP (backend) | integração (Spring, MockMvc) | autenticação por papel, códigos 401/403/409/422, formato de erro | cada endpoint: 1 feliz, 1 negado, 1 inválido |
| Pagamento externo | contrato com o fake | falha do externo impede a matrícula | fake e cliente HTTP passam o mesmo teste |
| Concorrência | integração | duas matrículas na última vaga: só uma vence (RNF03) | 1 teste por turma lotada |
| Frontend: componentes | Testing Library | rótulo, erro anunciado (`role=alert`), foco, estados de carregamento | por componente |
| Frontend: telas e rotas | Testing Library + API mockada | validação, erro do servidor, redirecionamento por sessão e papel | por tela, incluindo erro |
| Fluxo completo | E2E (uma vez, no fim) | login → matricular → cobrança → professor vê o aluno | 1 cenário |

**Não testamos:** getters/setters, código de framework, estilos. **Como conferimos que o teste presta:**
plantar um defeito (mutação manual) nas regras críticas e ver o teste falhar.
**Acessibilidade:** axe-core em cada tela (com formulários e painéis abertos) e testes de rótulo, foco e alertas; contraste conferido nos tokens (AA nos dois temas, `contorno` com 3:1).

**Onde estão os testes que só existem por causa de um bug real:** `ConcorrenciaTest` (a trava do banco, provada removendo-a), `SegurancaHttpTest` (o `csrf()` do teste esconde o cookie real, então cookies e CSRF são testados contra um servidor de verdade), `MatriculaTest.naoMatriculaDuasVezesNaMesmaTurma` (a checagem do domínio falhava com proxies do Hibernate, invisível nos testes sem banco) e `ErrosTest` (rota inexistente devolvia 500).
