package com.biometrics.api.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de Application — LlmDecisionDTO.
 *
 * Representa a decisão estruturada gerada pelo Treinador Virtual (LLM).
 * O modelo é instruído via System Prompt a retornar OBRIGATORIAMENTE
 * este schema JSON — dois campos fixos.
 *
 * Exemplo de resposta da LLM:
 * {
 *   "decisao": "DELOAD",
 *   "justificativaBiomecanica": "RPE 9/10 com tonelagem de 4800kg indica..."
 * }
 */
@Schema(description = "Decisão do Treinador Virtual (IA) baseada nos dados biomecânicos da sessão")
@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmDecisionDTO(

        @Schema(
            description = "Decisão curta do treinador",
            example = "DELOAD",
            allowableValues = {"DELOAD", "REDUÇÃO DE VOLUME", "MANTER VOLUME", "AUMENTAR VOLUME", "ERRO_LLM"}
        )
        String decisao,

        @Schema(
            description = "Justificativa técnica e biomecânica da decisão",
            example = "RPE 9/10 com tonelagem de 4800kg indica acúmulo de fadiga sistêmica. Recomenda-se semana de deload com 40-60% da tonelagem habitual para restaurar a capacidade adaptativa."
        )
        String justificativaBiomecanica

) {
    /** Cria uma decisão de erro quando a LLM não está disponível ou falha. */
    public static LlmDecisionDTO deErro(String mensagem) {
        return new LlmDecisionDTO(
                "ERRO_LLM",
                "Orquestrador LLM indisponível: " + mensagem
        );
    }
}
