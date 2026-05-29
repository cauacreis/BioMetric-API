package com.biometrics.api.infrastructure.groq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO de Request para a API do Groq (compatível com OpenAI Chat Completions).
 *
 * Payload enviado via POST para:
 * https://api.groq.com/openai/v1/chat/completions
 *
 * Usando records aninhados para modelar o JSON de forma imutável e type-safe.
 */
public record GroqRequest(

        String model,

        List<Message> messages,

        /** Força a LLM a retornar JSON puro — elimina markdown e texto livre */
        @JsonProperty("response_format")
        ResponseFormat responseFormat,

        /** Controla criatividade: 0.0 = determinístico, 1.0 = criativo */
        double temperature

) {

    /** Representa uma mensagem no histórico de conversa (system / user / assistant) */
    public record Message(
            String role,
            String content
    ) {}

    /** Instrui o modelo a retornar SOMENTE JSON válido na resposta */
    public record ResponseFormat(
            String type  // "json_object"
    ) {}

    // -------------------------------------------------------------------------
    //  Factory method — constrói o request pronto para o Treinador Virtual
    // -------------------------------------------------------------------------

    /**
     * Cria um GroqRequest completo para o endpoint do Treinador Virtual,
     * com system prompt fixo e a mensagem do atleta como user message.
     *
     * @param systemPrompt  Contexto e regras de negócio da LLM
     * @param userMessage   Dados da sessão formatados para análise
     * @param model         Modelo Groq a utilizar (ex: llama3-8b-8192)
     */
    public static GroqRequest ofTreinador(String systemPrompt, String userMessage, String model) {
        return new GroqRequest(
                model,
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user",   userMessage)
                ),
                new ResponseFormat("json_object"),
                0.3  // baixa temperatura = respostas mais consistentes e técnicas
        );
    }
}
