package br.edu.matricula.api.web;

import br.edu.matricula.api.servico.NaoEncontradoException;
import br.edu.matricula.dominio.RegraDeNegocioException;
import br.edu.matricula.pagamento.PagamentoIndisponivelException;
import jakarta.servlet.ServletException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Toda falha vira {@code {codigo, mensagem}}. Nada de stack trace nem detalhe interno na resposta. */
@RestControllerAdvice
class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);
    /** Códigos de dado malformado (422); os demais erros de regra são conflitos com o estado atual (409). */
    private static final Set<String> DADO_INVALIDO = Set.of("DADO_INVALIDO", "SEMESTRE_INVALIDO", "LIMITES_INVALIDOS", "SENHA_INVALIDA", "PAPEL_NAO_GERENCIAVEL");

    private static ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("codigo", codigo, "mensagem", mensagem));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    ResponseEntity<Map<String, Object>> regra(RegraDeNegocioException e) {
        return resposta(DADO_INVALIDO.contains(e.codigo()) ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.CONFLICT, e.codigo(), e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<Map<String, Object>> naoEncontrado(NaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.codigo(), e.getMessage());
    }

    @ExceptionHandler(PagamentoIndisponivelException.class)
    ResponseEntity<Map<String, Object>> pagamento(PagamentoIndisponivelException e) {
        log.warn("Sistema de pagamento indisponível: {}", e.getMessage());
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "PAGAMENTO_INDISPONIVEL",
                "O sistema de pagamento está indisponível e a operação não foi feita. Tente de novo em instantes.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException e) {
        var campos = new LinkedHashMap<String, String>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("codigo", "DADO_INVALIDO", "mensagem", "Confira os campos informados.", "campos", campos));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, Object>> requisicaoMalFeita(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "DADO_INVALIDO", "A requisição está incompleta ou mal formada.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException e) {
        log.warn("Violação de integridade: {}", e.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "CONFLITO", "Já existe um registro com esses dados.");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ResponseEntity<Map<String, Object>> trava(PessimisticLockingFailureException e) {
        return resposta(HttpStatus.CONFLICT, "OCUPADO", "O sistema está ocupado com outra operação sua. Tente de novo.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    void acessoNegado(AccessDeniedException e) {
        throw e; // a cadeia de segurança responde 403 no formato padrão
    }

    /** Rota inexistente (404), método ou tipo de conteúdo não aceitos (405, 415): erro de quem chamou, não do servidor. */
    @ExceptionHandler(ServletException.class)
    ResponseEntity<Map<String, Object>> rotaOuMetodo(ServletException e) {
        if (e instanceof ErrorResponse erro) {
            var status = HttpStatus.valueOf(erro.getStatusCode().value());
            return status == HttpStatus.NOT_FOUND
                    ? resposta(status, "NAO_ENCONTRADO", "Recurso não encontrado.")
                    : resposta(status, "REQUISICAO_INVALIDA", "A requisição não é aceita nesta rota.");
        }
        return inesperado(e);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Algo deu errado do nosso lado. Tente de novo.");
    }
}
