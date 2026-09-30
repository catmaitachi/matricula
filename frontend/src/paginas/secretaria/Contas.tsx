import { Fragment, useState, type FormEvent } from 'react'
import { api } from '../../api'
import { ROTULO_PAPEL } from '../../api/rotulos'
import type { Conta, Papel } from '../../api/tipos'
import { Plus } from 'lucide-react'
import { Botao } from '../../componentes/Botao'
import { Icone } from '../../componentes/Icone'
import { Campo } from '../../componentes/Campo'
import { Carga } from '../../componentes/Carga'
import { Linha, Linhas, Marca, Painel } from '../../componentes/Linhas'
import { Opcoes } from '../../componentes/Opcoes'
import { useAviso } from '../../componentes/contextoAviso'
import { useCarregar } from '../../componentes/useCarregar'
import formulario from '../../estilos/formulario.module.css'
import { Pagina } from '../../layout/Pagina'
import { camposDe, mensagemDe, pad2 } from '../../util'

type Gerenciavel = Exclude<Papel, 'SECRETARIA'>

interface FormProps {
  papel: Gerenciavel
  conta?: Conta
  aoConcluir: () => void
}

/** Cria (sem `conta`) ou edita uma conta. A senha nunca volta da API; ao editar, só se preenche para trocá-la. */
function FormularioConta({ papel, conta, aoConcluir }: FormProps) {
  const { avisar } = useAviso()
  const [numPessoa, setNumPessoa] = useState(conta?.numPessoa ?? '')
  const [nome, setNome] = useState(conta?.nome ?? '')
  const [numMatricula, setNumMatricula] = useState(conta?.numMatricula ?? '')
  const [senha, setSenha] = useState('')
  const [ativo, setAtivo] = useState(conta?.ativo ?? true)
  const [erros, setErros] = useState<Record<string, string>>({})
  const [enviando, setEnviando] = useState(false)

  async function salvar(e: FormEvent) {
    e.preventDefault()
    setEnviando(true)
    setErros({})
    try {
      if (conta) {
        await api.contas.atualizar(conta.id, {
          nome,
          ativo,
          numMatricula: papel === 'ALUNO' ? numMatricula : undefined,
          novaSenha: senha || undefined,
        })
        avisar('ok', `Conta de ${nome} atualizada.`)
      } else {
        await api.contas.criar({ papel, numPessoa, nome, senha, numMatricula: papel === 'ALUNO' ? numMatricula : undefined })
        avisar('ok', `Conta de ${nome} criada.`)
      }
      aoConcluir()
    } catch (erro) {
      const campos = camposDe(erro)
      if (Object.keys(campos).length > 0) setErros(campos)
      else avisar('erro', mensagemDe(erro))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className={formulario.formulario} onSubmit={(e) => void salvar(e)} noValidate aria-label={conta ? 'Editar conta' : 'Nova conta'}>
      <div className={formulario.colunas}>
        {!conta && (
          <Campo rotulo="Nº de pessoa" value={numPessoa} onChange={(e) => setNumPessoa(e.target.value)} erro={erros.numPessoa} required autoCapitalize="characters" spellCheck={false} />
        )}
        <Campo rotulo="Nome" value={nome} onChange={(e) => setNome(e.target.value)} erro={erros.nome} required autoComplete="off" />
        {papel === 'ALUNO' && (
          <Campo rotulo="Nº de matrícula" value={numMatricula} onChange={(e) => setNumMatricula(e.target.value)} erro={erros.numMatricula} required />
        )}
        <Campo
          rotulo={conta ? 'Nova senha (opcional, mín. 8)' : 'Senha inicial (mín. 8)'}
          type="password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          erro={erros.senha ?? erros.novaSenha}
          autoComplete="new-password"
          required={!conta}
        />
      </div>
      {conta && (
        <Opcoes
          legenda="Situação"
          nome="situacao"
          valor={ativo ? 'ativa' : 'desativada'}
          aoMudar={(v) => setAtivo(v === 'ativa')}
          opcoes={[
            { valor: 'ativa', rotulo: 'Ativa' },
            { valor: 'desativada', rotulo: 'Desativada' },
          ]}
        />
      )}
      <div className={formulario.acoes}>
        <Botao type="submit" carregando={enviando}>
          {enviando ? 'Salvando…' : 'Salvar'}
        </Botao>
        <Botao onClick={aoConcluir}>Cancelar</Botao>
      </div>
    </form>
  )
}

/** RF06: a secretaria mantém as contas de alunos e professores. Contas não são apagadas, só desativadas. */
export function Contas() {
  const [papel, setPapel] = useState<Gerenciavel>('ALUNO')
  const [carga, recarregar] = useCarregar(() => api.contas.listar(papel), [papel])
  const [editando, setEditando] = useState<number | 'nova' | null>(null)

  const concluir = () => {
    setEditando(null)
    recarregar()
  }

  return (
    <Pagina
      kicker="Secretaria"
      titulo="Contas"
      acoes={<Botao onClick={() => setEditando('nova')}>
          <Icone de={Plus} size={18} /> Nova conta
        </Botao>}
    >
      <Opcoes
        legenda="Mostrar"
        nome="papel"
        valor={papel}
        aoMudar={(v) => {
          setPapel(v)
          setEditando(null)
        }}
        opcoes={[
          { valor: 'ALUNO', rotulo: 'Alunos' },
          { valor: 'PROFESSOR', rotulo: 'Professores' },
        ]}
      />
      {editando === 'nova' && <FormularioConta papel={papel} aoConcluir={concluir} />}
      <Carga carga={carga} tentar={recarregar}>
        {(contas) =>
          contas.length === 0 ? (
            <p className={formulario.vazio}>Nenhuma conta de {ROTULO_PAPEL[papel].toLowerCase()} ainda.</p>
          ) : (
            <Linhas rotulo={`Contas de ${ROTULO_PAPEL[papel].toLowerCase()}`}>
              {contas.map((c, i) => (
                <Fragment key={c.id}>
                  <Linha indice={pad2(i + 1)} aoSelecionar={() => setEditando(editando === c.id ? null : c.id)} selecionada={editando === c.id}>
                    <strong role="cell">
                      {c.nome}
                      {!c.ativo && <Marca>desativada</Marca>}
                    </strong>
                    <span role="cell">{c.numPessoa}</span>
                    {c.numMatricula && (
                      <span role="cell" data-fim className={formulario.fim}>
                        Matrícula {c.numMatricula}
                      </span>
                    )}
                  </Linha>
                  {editando === c.id && (
                    <Painel>
                      <FormularioConta papel={papel} conta={c} aoConcluir={concluir} />
                    </Painel>
                  )}
                </Fragment>
              ))}
            </Linhas>
          )
        }
      </Carga>
    </Pagina>
  )
}
