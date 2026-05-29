package com.biometrics.api.infrastructure.web;

import com.biometrics.api.infrastructure.groq.exception.GroqIntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler Global de Exceções — GlobalExceptionHandler.
 *
 * Intercepta exceções lançadas em qualquer Controller e retorna
 * respostas HTTP padronizadas com estrutura JSON consistente.
 *
 * Tratamentos:
 *  - Bean Validation failures (400)
 *  - Falhas de integração com Groq (502/503/504)
 *  - Erros inesperados genéricos (500)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    //  400 — Erros de validação do payload (Bean Validation)
    // -------------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return ResponseEntity
                .badRequest()
                .body(buildErrorBody(HttpStatus.BAD_REQUEST, "Payload inválido", erros));
    }

    // -------------------------------------------------------------------------
    //  502/503/504 — Falhas de integração com o Groq
    // -------------------------------------------------------------------------
    @ExceptionHandler(GroqIntegrationException.class)
    public ResponseEntity<Map<String, Object>> handleGroqError(
            GroqIntegrationException ex) {

        log.error("Falha na integração Groq [{}]: {}", ex.getHttpStatus(), ex.getMessage());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(buildErrorBody(
                        ex.getHttpStatus(),
                        "Falha na integração com o Orquestrador LLM",
                        List.of(ex.getMessage())
                ));
    }

    // -------------------------------------------------------------------------
    //  500 — Erros inesperados
    // -------------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);

        return ResponseEntity
                .internalServerError()
                .body(buildErrorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno do servidor",
                        List.of("Contate o suporte: " + ex.getMessage())
                ));
    }

    // -------------------------------------------------------------------------
    //  Helper — estrutura padrão de resposta de erro
    // -------------------------------------------------------------------------
    private Map<String, Object> buildErrorBody(
            HttpStatus status, String mensagem, List<String> detalhes) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status",    status.value());
        body.put("erro",      status.getReasonPhrase());
        body.put("mensagem",  mensagem);
        body.put("detalhes",  detalhes);
        return body;
    }
}
