package com.biometrics.api.infrastructure.groq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO de Response da API do Groq (compatível com OpenAI Chat Completions).
 *
 * Estrutura retornada pelo endpoint POST /openai/v1/chat/completions.
 * @JsonIgnoreProperties(ignoreUnknown = true) garante que campos extras
 * retornados pela API (usage, id, etc.) não causem erros de desserialização.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GroqResponse(

        String id,
        List<Choice> choices

) {

    /** Cada choice contém uma mensagem gerada pelo modelo */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
            Message message,
            @com.fasterxml.jackson.annotation.JsonProperty("finish_reason")
            String finishReason
    ) {}

    /** Mensagem retornada pela LLM */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            String role,
            String content
    ) {}

    // -------------------------------------------------------------------------
    //  Helper — extrai o conteúdo textual da primeira choice com segurança
    // -------------------------------------------------------------------------

    /**
     * Retorna o conteúdo da primeira resposta gerada pela LLM.
     * Lança IllegalStateException se a resposta estiver vazia ou malformada.
     */
    public String extrairConteudo() {
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("Groq retornou resposta sem choices.");
        }
        Choice primeiraChoice = choices.get(0);
        if (primeiraChoice.message() == null || primeiraChoice.message().content() == null) {
            throw new IllegalStateException("Groq retornou choice sem conteúdo.");
        }
        return primeiraChoice.message().content();
    }
}
