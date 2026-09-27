# 🎓 Sistema de Matrículas

Sistema de matrículas acadêmicas modelado e implementado em **Java**, desenvolvido como trabalho da disciplina de **Laboratório de Desenvolvimento de Software**.

O sistema permite que a **secretaria** gere o currículo de cada semestre e mantenha os cadastros, que **alunos** se matriculem (e cancelem a matrícula) em disciplinas obrigatórias e optativas, que **professores** consultem os alunos de suas turmas e que um **sistema externo de pagamento** seja notificado para realizar as cobranças.

> ⚠️ **Status:** a base do projeto está implementada. As classes seguem o diagrama de classes e a maior parte das operações ainda são *stubs* (apenas imprimem no console o que fariam).

---

## 👥 Integrantes

| Nome |
| :--- |
| Isaías Alves |
| Laura Lara |
| Lucas Spiazzi |
| Davi Lage |

---

## 📋 Requisitos

### Funcionais

| ID | Descrição |
| :---: | :--- |
| RF01 | Secretaria mantém currículos |
| RF02 | Aluno mantém matrícula |
| RF03 | Professor verifica alunos matriculados |
| RF04 | Usuário realiza login |
| RF05 | Sistema de pagamento notifica aluno |
| RF06 | Secretaria mantém contas cadastradas |

### Não-funcionais

| ID | Descrição |
| :---: | :--- |
| RNF01 | O sistema de matrículas precisa se comunicar com um sistema externo já existente. |
| RNF02 | Todos os usuários do sistema têm senhas que são utilizadas para validação do login. |
| RNF03 | O sistema precisa estar no ar e estável durante o período de matrículas. |

Detalhes em [`requisitos.md`](requisitos.md).

---

## 🧑‍💻 Histórias de Usuário

| ID | Persona | Funcionalidade |
| :---: | :--- | :--- |
| US01 | Aluno | Fazer e desfazer matrícula em matérias obrigatórias e optativas |
| US02 | Professor | Visualizar os alunos de cada disciplina |
| US03 | Sistema de Pagamento | Receber avisos dos alunos que devem ser cobrados |
| US04 | Secretaria | Gerar o currículo das disciplinas do semestre |
| US05 | Secretaria | Manter as informações de todos os usuários |

Detalhes em [`historias_de_usuario.md`](historias_de_usuario.md).

---

## 📐 Modelagem

### Diagrama de Casos de Uso

![Casos de Uso](Casos%20de%20Uso%20Matr%C3%ADcula.png)

### Diagrama de Classes

![Diagrama de Classes](diagramaClasses/Diagrama%20v2.png)

<details>
<summary>Versão anterior (v1)</summary>

![Diagrama de Classes v1](diagramaClasses/Diagrama%20v1.png)

</details>

---

## 🧱 Estrutura das classes

| Classe | Papel |
| :--- | :--- |
| `Usuario` | Classe abstrata com nome, número de pessoa, senha e `realizarLogin()` |
| `Aluno` | Usuário que se matricula e cancela matrícula; limite de **6 disciplinas** |
| `Professor` | Usuário que leciona disciplinas e visualiza os alunos matriculados |
| `Secretaria` | Usuário que mantém alunos, professores e disciplinas e gera o currículo do semestre |
| `Curso` | Curso com nome, número de créditos e suas disciplinas |
| `Curriculo` | Conjunto de disciplinas ofertadas em um semestre |
| `Disciplina` | Disciplina com mínimo (padrão 3) e máximo (padrão 60) de alunos; valida se ocorre e encerra inscrições |
| `Matricula` | Liga um aluno a uma disciplina (obrigatória/optativa) e notifica o sistema de pagamento |
| `SistemaPagamento` | Interface para o sistema externo de cobrança |
| `SistemaPagamentoExterno` | Implementação da interface que simula o sistema externo |
| `Main` | Demonstração do fluxo completo do sistema |

---

## 📁 Estrutura do repositório

```
matricula/
├── java/                        # Código-fonte
├── diagramaClasses/             # Diagramas de classes (v1 e v2)
├── Casos de Uso Matrícula.png   # Diagrama de casos de uso
├── requisitos.md                # Requisitos funcionais e não-funcionais
├── historias_de_usuario.md      # Histórias de usuário
└── README.md
```

---

## ▶️ Como executar

Pré-requisito: **JDK 8+** instalado.

```bash
cd java
javac -d out *.java
java -cp out Main
```

A execução roda a demonstração do `Main`: login dos usuários, geração do currículo, criação de curso e disciplina, matrícula de um aluno, notificação ao sistema de pagamento e cancelamento da matrícula.

---

## 🛠️ Tecnologias

- Java
- UML (casos de uso e diagrama de classes)
