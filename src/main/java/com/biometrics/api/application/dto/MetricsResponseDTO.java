package com.biometrics.api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de Resposta — MetricsResponseDTO.
 *
 * Pertence à camada de Application: faz a ponte entre o domínio e o mundo externo.
 * Agora inclui a decisão real do Treinador Virtual (LLM Groq).
 */
@Schema(description = "Resposta da análise de métricas da sessão de treino com decisão do Treinador Virtual")
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

        @Schema(description = "Número de sets analisados", example = "8")
        Integer totalDeSets,

        @Schema(description = "Decisão e justificativa geradas pelo Treinador Virtual (LLM Groq)")
        LlmDecisionDTO decisaoTreinadorVirtual

) {}
