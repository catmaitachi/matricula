# 🎓 Sistema de Matrículas

Sistema de matrículas acadêmicas do trabalho de **Laboratório de Desenvolvimento de Software**. A **secretaria** monta o currículo de cada semestre e mantém as contas; **alunos** se matriculam e cancelam (até 4 obrigatórias e 2 optativas); **professores** veem os alunos das suas turmas; um **sistema de pagamento externo** é avisado e cobra a cada matrícula.

Antes um esqueleto em Java com métodos que só imprimiam no console (`java/`, ainda no histórico do git); agora um **backend Java 21 modular** e um **frontend React + TypeScript**, construídos etapa por etapa na ordem do fluxo do usuário.

## 👥 Integrantes

| Nome |
| :--- |
| Isaías Alves |
| Laura Lara |
| Lucas Spiazzi |
| Davi Lage |

## ▶️ Como executar

Pré-requisitos: **JDK 21**, **Maven 3.9** e **Node 22**.

```bash
# 1. backend (porta 8080). O perfil "dev" cria contas e dados de exemplo.
cd backend
mvn install                                            # compila e roda todos os testes
java -jar api/target/api-0.1.0.jar --spring.profiles.active=dev

# 2. frontend (porta 5173), em outro terminal
cd frontend
npm install
npm run dev                                            # http://localhost:5173
```

### Contas de teste (perfil `dev`)

Senhas públicas, **só para desenvolvimento**: existem apenas com `--spring.profiles.active=dev`.

| Papel | Nº de pessoa | Senha | Nome | O que mostra |
| :-- | :-- | :-- | :-- | :-- |
| Secretaria | `SEC001` | `senhaSec123` | Maria Silva | contas, disciplinas, currículo 2026/2 (abrir/encerrar) |
| Professor | `PROF100` | `senhaProf456` | Dr. Carlos Eduardo | 4 turmas: Engenharia de Software A, Lab. de Desenvolvimento A, Redes B, Banco de Dados A |
| Professor | `PROF101` | `senhaProf789` | Profa. Helena Duarte | 4 turmas: Cálculo II A, Redes A, Inteligência Artificial A, Seminário de Pesquisa A |
| Aluno | `ALU999` | `senhaAlu789` | João Pedro (matrícula 20261001) | sem matrículas: bom para testar matricular do zero |
| Aluno | `ALU001` | `senhaAlu123` | Ana Beatriz (20261002) | Eng. de Software (obrigatória) + Seminário e Redes B (optativas): **limite de optativas 2/2** |
| Aluno | `ALU002` | `senhaAlu123` | Bruno Costa (20261003) | igual à Ana |
| Aluno | `ALU003` | `senhaAlu123` | Carla Nunes (20261004) | só Eng. de Software (obrigatória) |

Situações prontas no semestre 2026/2 (aberto): **Engenharia de Software A** com quórum exato (3 alunos, o mínimo), **Seminário de Pesquisa A** lotada (2/2) e **Redes B** abaixo do mínimo (2/3, cai ao encerrar). As demais turmas estão vazias e também caem ao encerrar.

Para voltar aos dados de exemplo: pare o backend, apague `backend/data/` e suba de novo.

Fora do perfil `dev` não existe nenhuma conta e **não há senha padrão**. Para criar a primeira secretaria, defina a senha (12 caracteres ou mais) ao subir o servidor; ela só é usada se a conta ainda não existir, e as demais contas são criadas pela tela:

```bash
MATRICULA_BOOTSTRAP_SENHA='uma-senha-longa-e-secreta' java -jar api/target/api-0.1.0.jar
```

O perfil `dev` também cria o semestre 2026/2 aberto, com uma turma com quórum, uma lotada (2/2) e uma abaixo do mínimo (2/3), para ver cada situação na tela. Sem o perfil `dev` o banco começa vazio e nenhuma conta é criada.

## 🧱 Estrutura

```
matricula/
├── backend/            Maven multi-módulo, Java 21, Spring Boot 4
│   ├── dominio/        entidades e regras de negócio (sem Spring)
│   ├── pagamento/      porta do sistema externo (RNF01) + simulação
│   └── api/            REST, segurança, persistência (H2), casos de uso
├── frontend/           React 19 + TypeScript + Vite
├── scripts/e2e.py      cenário ponta a ponta contra o jar
├── .design/            decisões do /inspiration (conceito e páginas); sem design system global
├── docs/               requisitos, ADRs, API, estratégia de testes
└── diagramaClasses/, Casos de Uso Matrícula.png   modelagem UML
```

Por que módulos: `dominio` não conhece web nem banco (dá para testar as regras sem subir nada), e `pagamento` isola o sistema externo atrás de uma interface (trocar a simulação por um cliente HTTP real não toca no resto). Detalhes em [`docs/adr/`](docs/adr).

## ✅ Requisitos → onde estão

| Requisito | Onde | Testes |
| :-- | :-- | :-- |
| **RF01** secretaria mantém currículos | `Curriculo`, `CurriculoServico`; telas Currículos e Disciplinas | `CurriculoTest`, `OfertaTest`, `EncerramentoTest` |
| **RF02** aluno mantém matrícula | `Aluno.matricular/cancelar`, `MatriculaServico`; telas Currículo e Minhas matrículas | `AlunoMatriculaTest`, `MatriculaTest`, `ConcorrenciaTest` |
| **RF03** professor vê os alunos | `ProfessorServico`; telas Minhas turmas e lista de chamada | `ProfessorTest` |
| **RF04** login | Spring Security (sessão em cookie); tela de entrada | `SegurancaTest`, `SegurancaHttpTest` |
| **RF05** pagamento | `SistemaPagamento`, `CobrancaServico`, `ReconciliadorDeCobrancas` | `PagamentoFakeTest`, `MatriculaTest`, `EncerramentoTest` |
| **RF06** secretaria mantém contas | `ContaServico`; tela Contas | `ContasTest` |
| **RNF01** sistema externo | módulo `pagamento` | `PagamentoFakeTest` |
| **RNF02** senhas | bcrypt (`{bcrypt}`), limite de 72 bytes | `ContasTest`, `SegurancaTest` |
| **RNF03** estável no período | trava por aluno e por turma; reconciliador; `/actuator/health` | `ConcorrenciaTest` |

Requisitos detalhados, regras e decisões: [`docs/requisitos.md`](docs/requisitos.md). Referência da API: [`docs/api.md`](docs/api.md).

## 🔒 Segurança

- Sessão em cookie `HttpOnly; Secure; SameSite=Strict`; **nada da sessão fica no JavaScript** (sem `localStorage`/`sessionStorage`).
- Proteção CSRF com token em cookie legível, devolvido no cabeçalho `X-XSRF-TOKEN` em toda escrita.
- Senhas em bcrypt; bloqueio após 5 erros seguidos; mesma resposta para senha errada e conta inexistente.
- Autorização por papel **e por objeto** (aluno só age em nome de si; professor só vê suas turmas).
- Erros sempre `{codigo, mensagem}`, sem pilha nem SQL; cabeçalhos de segurança em toda resposta.
- Travas de banco contra vaga vendida duas vezes e contra furar o limite de um aluno.

Ver [`docs/adr/0003-seguranca.md`](docs/adr/0003-seguranca.md) para as decisões e os limites conhecidos.

## 🧪 Qualidade

```bash
cd backend  && mvn test          # 93 testes: 19 domínio, 5 pagamento, 69 da API (segurança, fluxos, concorrência)
cd frontend && npm test          # 111 testes, inclui acessibilidade (axe) em todas as telas
cd frontend && npm run typecheck && npm run lint && npm run build
```

E um cenário ponta a ponta (26 passos, do login da secretaria ao encerramento do semestre) contra o jar empacotado, com só a secretaria inicial:

```bash
MATRICULA_BOOTSTRAP_SENHA=x-uma-senha-longa java -jar backend/api/target/api-0.1.0.jar --server.port=8081 --spring.datasource.url=jdbc:h2:mem:e2e &
E2E_SENHA_SECRETARIA=x-uma-senha-longa python3 scripts/e2e.py http://localhost:8081
```

Estratégia em [`docs/estrategia-de-testes.md`](docs/estrategia-de-testes.md).

## 📐 Modelagem

Casos de uso e classes (v1 e v2) estão na raiz e em `diagramaClasses/`. A `Turma` do diagrama v2 foi implementada: a matrícula é em uma turma de uma disciplina, e os limites de 3 a 60 alunos valem por turma.

## 🤝 Para quem vai mexer no código

[`CLAUDE.md`](CLAUDE.md) resume as regras do projeto (segurança, domínio, design), os comandos e as armadilhas já encontradas. Vale para o grupo e para o Claude Code.

## 🛠️ Tecnologias

Java 21 · Spring Boot 4.1 (Web, Data JPA, Security, Validation, Actuator) · H2 · JUnit 5 · React 19 · TypeScript · Vite · Vitest · Testing Library · axe-core · CSS Modules.
