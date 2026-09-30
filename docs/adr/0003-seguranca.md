# ADR-0003: Segurança da sessão, do acesso e dos dados

**Status:** Aceito · **Data:** 2026-09-29 · **Decisores:** grupo do laboratório

## Contexto
O grupo exigiu boas práticas mesmo sendo um trabalho de laboratório. A primeira versão do front guardava a sessão em `sessionStorage`, que qualquer script injetado consegue ler; foi rejeitada.

## Decisão
| Ponto | Decisão | Por quê |
| :-- | :-- | :-- |
| Sessão | Sessão no servidor, cookie `MATRICULA_SESSAO` com `HttpOnly; Secure; SameSite=Strict`, 30 min; ID trocado no login | O JavaScript não enxerga a sessão; não há token para roubar |
| CSRF | Token em cookie legível (`XSRF-TOKEN`) devolvido em `X-XSRF-TOKEN` em toda escrita, inclusive login e logout | Um site de fora não lê o cookie, então não consegue montar o cabeçalho |
| Sessões anônimas | Cache de requisição desligado | Sem isto cada 401 criaria uma sessão no servidor |
| Senha | bcrypt via `DelegatingPasswordEncoder` (prefixo `{bcrypt}`), mínimo de 8 e máximo de 72 bytes | Algoritmo trocável sem invalidar senhas; o bcrypt trunca em 72 bytes, então recusamos em vez de truncar |
| Força bruta | 5 erros seguidos bloqueiam o nº de pessoa por 15 min, exista a conta ou não; mesma resposta para senha errada e conta inexistente | Não revela quais contas existem |
| Conta desativada | Um filtro confere a cada requisição se o usuário continua ativo | Sem isso a sessão já aberta continuaria valendo |
| Autorização | Por papel (regras de rota) **e por objeto** (o id do aluno/professor vem da sessão, nunca do pedido; recurso alheio responde 404) | Impede o acesso ao dado de outra pessoa (IDOR) |
| Erros | `{codigo, mensagem}` fixos; sem pilha, classe nem SQL | Não vaza detalhe interno |
| Cabeçalhos | `Content-Security-Policy: default-src 'none'`, `X-Frame-Options: DENY`, `nosniff`, `Referrer-Policy: no-referrer`, `Cache-Control: no-store` | Respostas da API nunca são renderizadas nem guardadas |
| Contas de exemplo | Só com `--spring.profiles.active=dev`, com aviso no log; sem o perfil o banco nasce vazio | Não há senha conhecida fora do desenvolvimento |
| Banco | Só consultas parametrizadas (JPA); H2 console desligado; `actuator` expõe só `health` | Sem injeção de SQL nem painéis abertos |

## Limites conhecidos (não resolvidos de propósito)
- O bloqueio de login e as sessões ficam em memória: com mais de uma instância seria preciso um armazenamento compartilhado (Spring Session, Redis).
- O bloqueio é por nº de pessoa, então quem digitar 5 senhas erradas trava aquela conta por 15 min (troca de segurança por disponibilidade); não há limite por IP.
- Trocar a senha de alguém não derruba as sessões abertas dele (a desativação derruba).
- O front é servido por outro processo (Vite em desenvolvimento). Em produção, o servidor que entregar os arquivos estáticos precisa enviar seu próprio `Content-Security-Policy` e `frame-ancestors`.
- `Secure` exige HTTPS fora de `localhost`.
- Não há recuperação de senha nem registro de auditoria.

## Como foi verificado
Testes automatizados (`SegurancaTest`, `SegurancaHttpTest` contra servidor real com cookies e CSRF reais, `ContasTest`, `MatriculaTest`, `ProfessorTest`, `ErrosTest`), sondagem manual com `curl` contra o servidor rodando, `npm audit` sem vulnerabilidades e verificação de que o bundle de produção não contém senhas de exemplo.
