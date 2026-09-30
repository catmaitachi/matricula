# Requisitos detalhados

Os requisitos originais estão em [`../requisitos.md`](../requisitos.md) e as histórias em [`../historias_de_usuario.md`](../historias_de_usuario.md). Aqui cada um vira regras verificáveis, na ordem do fluxo do usuário (que é também a ordem em que o sistema foi construído).

| Etapa | Ator | Req. | O que precisa valer |
| :-- | :-- | :-- | :-- |
| **E1 Login** | Todos | RF04, RNF02 | Entra com nº de pessoa e senha; senha guardada só como hash; cada papel cai na sua tela; rota de outro papel é recusada (403). Após 5 erros seguidos a conta é bloqueada por 15 min. |
| **E2 Contas** | Secretaria | RF06, US05 | Cria, edita e desativa alunos e professores; nº de pessoa e nº de matrícula únicos; a senha nunca volta da API; conta desativada perde o acesso na hora. |
| **E3 Oferta** | Secretaria | RF01, US04 | Cursos e disciplinas (mín ≤ máx, padrão 3 e 60). Um currículo por semestre (`AAAA/1` ou `AAAA/2`), montado em rascunho com turmas (disciplina, professor, turno) e depois aberto. Só um semestre aberto por vez. |
| **E4 Matrícula** | Aluno | RF02, US01 | Escolhe uma turma do semestre aberto e o tipo (obrigatória ou optativa). Recusa: semestre não aberto, mesma disciplina já cursada, turma lotada, mais de **4 obrigatórias** ou de **2 optativas**. Cancelar só com o semestre aberto. |
| **E5 Cobrança** | Sistema de pagamento | RF05, US03, RNF01 | Cada matrícula avisa e cobra no sistema externo (idempotente); se ele falhar, a matrícula não se efetiva (503). Cancelar a matrícula ou perder a turma cancela a cobrança. O aluno vê a situação da cobrança. |
| **E6 Professor** | Professor | RF03, US02 | Vê só as turmas dele e os alunos matriculados em cada uma; turma alheia responde como inexistente. |
| **E7 Encerramento** | Secretaria | (regra do domínio) | Encerrar o semestre confirma turmas com quórum (≥ mínimo) e cancela as demais junto com suas matrículas e cobranças; depois disso ninguém se matricula nem cancela. |

**RNF03 (estável no período de matrículas)** virou: a última vaga nunca é vendida duas vezes e um aluno nunca fura o limite mesmo com cliques simultâneos (trava por linha no banco, testada com threads); cobranças que ficaram pela metade são refeitas sozinhas (`ReconciliadorDeCobrancas`); há `/actuator/health`.

## Decisões e divergências entre os documentos

| Assunto | O que os documentos diziam | O que foi feito |
| :-- | :-- | :-- |
| Turma | Está no diagrama v2, não estava no código | Implementada. Limites 3–60 valem por turma; matrícula é em turma |
| RF05 × US03 | RF05: "pagamento notifica aluno". US03 e caso de uso: o pagamento **recebe** o aviso | Seguiu a US03; o aluno vê a situação da cobrança (cobre as duas leituras) |
| Limite | Código: 6 no total | Decisão do grupo: até 4 obrigatórias e 2 optativas |
| Falha do pagamento | Não especificado | Sem cobrança não há matrícula. Falha ao cancelar não perde nada: a matrícula segue e o cancelamento é refeito |
| Cancelar | Diagrama só tem avisar e cobrar | A interface ganhou `cancelarCobranca` |
| Tipo da matrícula | `String` no código, Enum no diagrama | Enum `OBRIGATORIA` / `OPTATIVA`, escolhido na matrícula |
| Semestre | `"2026/2"` fixo no código | Escolhido pela secretaria |
| Senha | Texto puro | bcrypt |

## Fora do escopo

Recuperação de senha, notas e presença, pagamento real (existe só a porta e a simulação), alta disponibilidade e envio de e-mail.
