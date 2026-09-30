# API

Base: `/api`. JSON, exceto `POST /auth/entrar` (formulário). Toda falha volta como `{ "codigo", "mensagem" }` (validação de formulário acrescenta `campos`). Escritas exigem o cabeçalho `X-XSRF-TOKEN` com o valor do cookie `XSRF-TOKEN`.

| Método e caminho | Papel | O que faz | Erros típicos |
| :-- | :-- | :-- | :-- |
| `POST /auth/entrar` (`numPessoa`, `senha`) | público | abre a sessão (204) | 401 `CREDENCIAIS_INVALIDAS`, 429 `MUITAS_TENTATIVAS` |
| `POST /auth/sair` | logado | encerra a sessão (204) | |
| `GET /auth/eu` | logado | quem sou eu | 401 `NAO_AUTENTICADO` |
| `GET /contas?papel=ALUNO\|PROFESSOR` | secretaria | lista contas | 422 `PAPEL_NAO_GERENCIAVEL` |
| `POST /contas` · `PUT /contas/{id}` | secretaria | cria / edita (inclui desativar e trocar senha) | 409 `NUM_PESSOA_EM_USO`, `NUM_MATRICULA_EM_USO`; 400 `DADO_INVALIDO` |
| `GET /cursos` · `POST /cursos` | secretaria | cursos | 409 `NOME_EM_USO` |
| `GET /disciplinas` · `POST` · `PUT /disciplinas/{id}` | secretaria | disciplinas e limites | 422 `LIMITES_INVALIDOS`; 409 `DISCIPLINA_EM_USO` |
| `GET /curriculos` · `POST` · `GET /curriculos/{id}` | secretaria | semestres | 422 `SEMESTRE_INVALIDO`; 409 `SEMESTRE_EM_USO` |
| `POST /curriculos/{id}/turmas` · `DELETE …/turmas/{turmaId}` | secretaria | monta o rascunho | 409 `CURRICULO_FECHADO_PARA_EDICAO`, `DISCIPLINA_INATIVA` |
| `POST /curriculos/{id}/abrir` | secretaria | abre as inscrições | 409 `CURRICULO_VAZIO`, `JA_HA_CURRICULO_ABERTO` |
| `POST /curriculos/{id}/encerrar` | secretaria | encerra; cancela turmas sem quórum | 409 `CURRICULO_NAO_ABERTO` |
| `GET /aluno/curriculo` | aluno | turmas do semestre aberto, com vagas | 404 `SEM_CURRICULO_ABERTO` |
| `GET /aluno/matriculas` | aluno | minhas matrículas, contagens e cobrança | |
| `POST /aluno/matriculas` (`turmaId`, `tipo`) | aluno | matricula e cobra | 409 `JA_MATRICULADO`, `TURMA_LOTADA`, `LIMITE_OBRIGATORIAS`, `LIMITE_OPTATIVAS`, `INSCRICOES_ENCERRADAS`; 503 `PAGAMENTO_INDISPONIVEL` |
| `DELETE /aluno/matriculas/{id}` | aluno | cancela e cancela a cobrança | 404 se não for sua; 409 `INSCRICOES_ENCERRADAS`; 503 `PAGAMENTO_INDISPONIVEL` |
| `GET /professor/turmas` | professor | minhas turmas | |
| `GET /professor/turmas/{id}/alunos` | professor | lista de chamada | 404 se a turma não for sua |
| `GET /actuator/health` | público | saúde | |
