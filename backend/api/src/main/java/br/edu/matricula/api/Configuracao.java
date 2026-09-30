package br.edu.matricula.api;

import br.edu.matricula.pagamento.PagamentoFake;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class Configuracao {

    /** bcrypt com prefixo {bcrypt}: o formato permite trocar o algoritmo no futuro sem invalidar senhas antigas. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    /** Sistema externo simulado. Um cliente HTTP real substituiria este bean, pois a porta é a mesma. */
    @Bean
    PagamentoFake sistemaPagamento(@Value("${matricula.pagamento.fake.indisponivel}") boolean indisponivel) {
        return new PagamentoFake(indisponivel);
    }
}
