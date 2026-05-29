package com.biometrics.api.infrastructure.groq.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção de Infraestrutura — GroqIntegrationException.
 *
 * Lançada quando a integração com a API do Groq falha por qualquer motivo:
 * - Chave de API inválida (401)
 * - Rate limit excedido (429)
 * - Modelo indisponível (503)
 * - Timeout de rede
 * - JSON malformado na resposta
 *
 * Carrega o HTTP status code original para que o GlobalExceptionHandler
 * possa retornar uma resposta apropriada ao cliente.
 */
public class GroqIntegrationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public GroqIntegrationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public GroqIntegrationException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
