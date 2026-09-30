# ADR-0002: Backend modular em Java

**Status:** Aceito · **Data:** 2026-09-29 · **Decisores:** grupo do laboratório

## Contexto
O código original eram classes com métodos que só imprimiam no console, num único diretório e sem persistência, testes ou limites reais. O sistema precisa de regras de negócio verificáveis (limite de matrículas, vagas, quórum), de um sistema externo de pagamento isolado (RNF01) e de resistir a cliques simultâneos na última vaga (RNF03).

## Decisão
Maven multi-módulo, Java 21 e Spring Boot 4.1 (a 3.5 já saiu do suporte aberto):

- **`dominio`**: entidades JPA com as regras dentro delas (`Aluno.matricular`, `Curriculo.encerrar`). Só depende das anotações `jakarta.persistence`; nenhum Spring.
- **`pagamento`**: a interface `SistemaPagamento` (`receberAvisoDeMatricula`, `cobrarAluno`, `cancelarCobranca`, todas idempotentes por chave) e `PagamentoFake`. Não depende do domínio.
- **`api`**: REST, segurança, repositórios, casos de uso e a orquestração entre domínio e pagamento.

Persistência em H2 em arquivo com `ddl-auto=update`. Concorrência com trava pessimista (`SELECT … FOR UPDATE`) no aluno e na turma. A matrícula grava com cobrança pendente, **fora** da transação avisa o sistema externo e, se ele falhar, desfaz; um reconciliador periódico refaz o que ficou pela metade.

## Alternativas
| Opção | Prós | Contras |
| :-- | :-- | :-- |
| **3 módulos, domínio com anotações JPA** (escolhida) | pouco código; regras testáveis sem Spring | o domínio conhece as anotações de mapeamento |
| Domínio puro + entidades JPA separadas + mapeadores | domínio 100% limpo | dobra as classes e o código de conversão, sem ganho neste porte |
| Projeto único | mais simples | mistura regra, web e banco; o sistema externo deixa de ser um ponto de troca |
| Chamar o pagamento dentro da transação | menos código | prende a trava do banco durante uma chamada de rede |

## Consequências
- Fácil: testar as regras com objetos comuns (19 testes de domínio, sem banco); trocar `PagamentoFake` por um cliente HTTP sem tocar no resto.
- Um bug só aparece com banco (proxies do Hibernate): por isso as regras têm testes nos dois níveis.
- Sem migrações de esquema (`ddl-auto=update`): serve ao laboratório; um ambiente real pediria Flyway ou Liquibase.
- O reconciliador roda em memória; com várias instâncias seria preciso coordenar (ex.: ShedLock).
