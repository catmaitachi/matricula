#!/usr/bin/env python3
"""Cenário ponta a ponta contra um backend no ar, do jeito que o navegador fala: cookies e CSRF de verdade.

  MATRICULA_BOOTSTRAP_SENHA=... java -jar backend/api/target/api-0.1.0.jar --server.port=8081 --spring.datasource.url=jdbc:h2:mem:e2e
  E2E_SENHA_SECRETARIA=... python3 scripts/e2e.py http://localhost:8081

Precisa de um backend sem dados (a secretaria SEC001 criada pela variável de ambiente) e do semestre 2099/1 livre.
Só usa a biblioteca padrão. Sai com código 1 se algum passo falhar.
"""
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

BASE = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8081").rstrip("/")
falhas = []


class Cliente:
    """Guarda os cookies à mão: o cookie de sessão é `Secure`, e clientes HTTP recusam mandá-lo por http://."""

    def __init__(self):
        self.cookies = {}

    def chamar(self, metodo, caminho, json_=None, form=None, csrf=True):
        cabecalhos = {"Accept": "application/json"}
        corpo = None
        if json_ is not None:
            corpo, cabecalhos["Content-Type"] = json.dumps(json_).encode(), "application/json"
        elif form is not None:
            corpo, cabecalhos["Content-Type"] = urllib.parse.urlencode(form).encode(), "application/x-www-form-urlencoded"
        if metodo != "GET" and csrf and "XSRF-TOKEN" in self.cookies:
            cabecalhos["X-XSRF-TOKEN"] = self.cookies["XSRF-TOKEN"]
        if self.cookies:
            cabecalhos["Cookie"] = "; ".join(f"{k}={v}" for k, v in self.cookies.items())
        pedido = urllib.request.Request(BASE + "/api" + caminho, data=corpo, headers=cabecalhos, method=metodo)
        try:
            resposta = urllib.request.urlopen(pedido)
        except urllib.error.HTTPError as erro:
            resposta = erro
        for c in resposta.headers.get_all("Set-Cookie") or []:
            nome, _, resto = c.partition("=")
            self.cookies[nome] = resto.split(";")[0]
        texto = resposta.read().decode()
        return resposta.status, (json.loads(texto) if texto else None)

    def entrar(self, num, senha):
        self.chamar("GET", "/auth/eu")  # traz o cookie CSRF
        return self.chamar("POST", "/auth/entrar", form={"numPessoa": num, "senha": senha})[0]


def confere(nome, obtido, esperado):
    ok = obtido == esperado
    print(f"{'ok  ' if ok else 'FALHA'} {nome}" + ("" if ok else f"  (esperado {esperado!r}, veio {obtido!r})"))
    if not ok:
        falhas.append(nome)


SENHA = "senha-e2e-1234"
sec = Cliente()
confere("visitante recebe 401", sec.chamar("GET", "/auth/eu")[0], 401)
confere("login sem token CSRF é recusado", Cliente().chamar("POST", "/auth/entrar", form={"numPessoa": "SEC001", "senha": "x"}, csrf=False)[0], 403)
confere("secretaria entra", sec.entrar("SEC001", os.environ["E2E_SENHA_SECRETARIA"]), 204)

# --- E2/E3: a secretaria monta tudo pela API
_, curso = sec.chamar("POST", "/cursos", {"nome": "E2E Curso", "numCreditos": 100})
_, redes = sec.chamar("POST", "/disciplinas", {"nome": "E2E Redes", "minAlunos": 2, "maxAlunos": 2, "ativa": True, "cursoIds": [curso["id"]]})
_, calculo = sec.chamar("POST", "/disciplinas", {"nome": "E2E Cálculo", "minAlunos": 3, "maxAlunos": 60, "ativa": True, "cursoIds": []})
_, prof = sec.chamar("POST", "/contas", {"papel": "PROFESSOR", "numPessoa": "EPROF", "nome": "Prof E2E", "senha": SENHA})
alunos = [sec.chamar("POST", "/contas", {"papel": "ALUNO", "numPessoa": f"EALU{i}", "nome": f"Aluno {i}", "senha": SENHA, "numMatricula": f"E2E{i}"})[1] for i in (1, 2, 3)]
confere("contas criadas sem devolver senha", "senha" in prof or "senhaHash" in prof, False)
confere("nº de pessoa repetido é recusado", sec.chamar("POST", "/contas", {"papel": "ALUNO", "numPessoa": "EALU1", "nome": "X", "senha": SENHA, "numMatricula": "Z"})[0], 409)
_, cur = sec.chamar("POST", "/curriculos", {"semestre": "2099/1"})
_, t_redes = sec.chamar("POST", f"/curriculos/{cur['id']}/turmas", {"disciplinaId": redes["id"], "professorId": prof["id"], "turno": "NOITE"})
_, t_calc = sec.chamar("POST", f"/curriculos/{cur['id']}/turmas", {"disciplinaId": calculo["id"], "professorId": prof["id"], "turno": "MANHA"})
confere("abrir as inscrições", sec.chamar("POST", f"/curriculos/{cur['id']}/abrir")[1]["estado"], "ABERTO")

# --- E4/E5: alunos se matriculam; o pagamento é cobrado
a1, a2, a3 = Cliente(), Cliente(), Cliente()
for c, i in ((a1, 1), (a2, 2), (a3, 3)):
    confere(f"aluno {i} entra", c.entrar(f"EALU{i}", SENHA), 204)
confere("aluno vê as 2 turmas", len(a1.chamar("GET", "/aluno/curriculo")[1]["turmas"]), 2)
s, m1 = a1.chamar("POST", "/aluno/matriculas", {"turmaId": t_redes["id"], "tipo": "OBRIGATORIA"})
confere("matrícula criada com cobrança enviada", (s, m1["cobranca"]), (201, "ENVIADA"))
confere("segundo aluno na mesma turma", a2.chamar("POST", "/aluno/matriculas", {"turmaId": t_redes["id"], "tipo": "OBRIGATORIA"})[0], 201)
confere("turma lotada recusa o terceiro", a3.chamar("POST", "/aluno/matriculas", {"turmaId": t_redes["id"], "tipo": "OPTATIVA"})[1]["codigo"], "TURMA_LOTADA")
confere("mesma turma duas vezes", a1.chamar("POST", "/aluno/matriculas", {"turmaId": t_redes["id"], "tipo": "OPTATIVA"})[1]["codigo"], "JA_MATRICULADO")
_, mc1 = a1.chamar("POST", "/aluno/matriculas", {"turmaId": t_calc["id"], "tipo": "OPTATIVA"})
a2.chamar("POST", "/aluno/matriculas", {"turmaId": t_calc["id"], "tipo": "OPTATIVA"})
confere("aluno cancela e a matrícula some", (a1.chamar("DELETE", f"/aluno/matriculas/{mc1['id']}")[0], len(a1.chamar("GET", "/aluno/matriculas")[1]["matriculas"])), (204, 1))
confere("aluno não cancela matrícula de outro", a3.chamar("DELETE", f"/aluno/matriculas/{m1['id']}")[0], 404)
a1.chamar("POST", "/aluno/matriculas", {"turmaId": t_calc["id"], "tipo": "OPTATIVA"})
confere("aluno não acessa área da secretaria", a1.chamar("GET", "/contas?papel=ALUNO")[0], 403)
confere("escrita sem token CSRF é recusada", a1.chamar("POST", "/aluno/matriculas", {"turmaId": t_calc["id"], "tipo": "OPTATIVA"}, csrf=False)[0], 403)

# --- E6: o professor vê os alunos
p = Cliente()
confere("professor entra", p.entrar("EPROF", SENHA), 204)
confere("professor vê as duas turmas", len(p.chamar("GET", "/professor/turmas")[1]), 2)
confere("professor vê os 2 alunos de Redes", sorted(a["nome"] for a in p.chamar("GET", f"/professor/turmas/{t_redes['id']}/alunos")[1]), ["Aluno 1", "Aluno 2"])
confere("professor não acessa a área do aluno", p.chamar("GET", "/aluno/matriculas")[0], 403)

# --- E7: encerramento
r = sec.chamar("POST", f"/curriculos/{cur['id']}/encerrar")[1]
confere("cancela a turma sem quórum (Cálculo: 2 de 3) e suas matrículas", (r["turmasCanceladas"], r["matriculasCanceladas"]), (1, 2))
_, minhas = a1.chamar("GET", "/aluno/matriculas")
confere("aluno vê a turma caída por falta de quórum", sorted((x["disciplina"], x["estado"]) for x in minhas["matriculas"]),
        [("E2E Cálculo", "CANCELADA_SEM_QUORUM"), ("E2E Redes", "ATIVA")])
confere("depois de encerrado ninguém se matricula", a3.chamar("POST", "/aluno/matriculas", {"turmaId": t_redes["id"], "tipo": "OPTATIVA"})[0], 409)

# --- sair
confere("sair encerra a sessão", (a1.chamar("POST", "/auth/sair")[0], a1.chamar("GET", "/auth/eu")[0]), (204, 401))

print(f"\n{'TUDO OK' if not falhas else str(len(falhas)) + ' FALHA(S): ' + ', '.join(falhas)}")
sys.exit(1 if falhas else 0)
