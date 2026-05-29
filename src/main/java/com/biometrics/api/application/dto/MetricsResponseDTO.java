package com.biometrics.api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de Resposta — MetricsResponseDTO.
 *
 * Pertence à camada de Application: faz a ponte entre o domínio e o mundo externo.
 * Encapsula o resultado do cálculo de volume e a mensagem do Orquestrador (mock).
 *
 * Usando Java Record para imutabilidade garantida em tempo de compilação.
 */
@Schema(description = "Resposta da análise de métricas da sessão de treino")
public record MetricsResponseDTO(

        @Schema(description = "ID do atleta analisado", example = "atleta-001")
        String atletaId,

        @Schema(description = "Data da sessão analisada", example = "2026-05-29")
        String dataSessao,

        @Schema(description = "Tonelagem Total da sessão em kg (Σ carga × repetições)", example = "4800.0")
        Double tonelagemTotalKg,

        @Schema(description = "Tonelagem ajustada pelo RPE — base para decisão de Deload", example = "3840.0")
        Double tonelagemAjustadaPorRpeKg,

        @Schema(description = "RPE registrado na sessão (1-10)", example = "8")
        Integer rpeSessao,

        @Schema(description = "Número de sets analisados", example = "12")
        Integer totalDeSets,

        @Schema(
            description = "Mensagem do Orquestrador LLM (mock até integração real)",
            example = "Fadiga registrada. Aguardando Orquestrador LLM para decisão de Deload."
        )
        String mensagemOrquestrador

) {
    /** Constante da mensagem mock do Orquestrador LLM — evita string literal duplicada. */
    public static final String MSG_AGUARDANDO_LLM =
            "Fadiga registrada. Aguardando Orquestrador LLM para decisão de Deload.";
}
