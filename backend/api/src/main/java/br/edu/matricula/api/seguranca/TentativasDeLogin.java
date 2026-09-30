package br.edu.matricula.api.seguranca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Freia a adivinhação de senha: depois de {@code maxFalhas} erros seguidos para um nº de pessoa, ele fica
 * bloqueado por {@code bloqueio}. A contagem é por nome informado, exista a conta ou não, para não revelar quais existem.
 * Fica em memória (uma instância); com várias instâncias seria preciso um armazenamento compartilhado.
 */
@Component
public class TentativasDeLogin {

    private static final int CAPACIDADE = 10_000;

    private record Registro(int falhas, Instant bloqueadoAte) {
    }

    private final Clock relogio;
    private final int maxFalhas;
    private final Duration bloqueio;
    // limitado: um atacante não consegue encher a memória com nomes inventados
    private final Map<String, Registro> registros = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Registro> eldest) {
            return size() > CAPACIDADE;
        }
    };

    public TentativasDeLogin(Clock relogio,
                             @Value("${matricula.login.max-falhas}") int maxFalhas,
                             @Value("${matricula.login.bloqueio-minutos}") long bloqueioMinutos) {
        this.relogio = relogio;
        this.maxFalhas = maxFalhas;
        this.bloqueio = Duration.ofMinutes(bloqueioMinutos);
    }

    private static String chave(String numPessoa) {
        return numPessoa == null ? "" : numPessoa.strip().toLowerCase(Locale.ROOT);
    }

    public synchronized boolean bloqueado(String numPessoa) {
        var registro = registros.get(chave(numPessoa));
        return registro != null && registro.bloqueadoAte() != null && relogio.instant().isBefore(registro.bloqueadoAte());
    }

    public synchronized void falhou(String numPessoa) {
        var k = chave(numPessoa);
        var atual = registros.get(k);
        var agora = relogio.instant();
        // bloqueio que já venceu recomeça a contagem
        var falhas = atual == null || (atual.bloqueadoAte() != null && !agora.isBefore(atual.bloqueadoAte())) ? 1 : atual.falhas() + 1;
        registros.put(k, new Registro(falhas, falhas >= maxFalhas ? agora.plus(bloqueio) : null));
    }

    public synchronized void sucesso(String numPessoa) {
        registros.remove(chave(numPessoa));
    }
}
