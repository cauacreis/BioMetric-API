package com.biometrics.api.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidade de Domínio Pura — WorkoutSession (Aggregate Root).
 *
 * Representa uma sessão completa de treino de um atleta.
 * É o Aggregate Root: toda operação de volume parte daqui.
 * Deliberadamente sem anotações JPA: o domínio não conhece a infraestrutura.
 */
@Schema(description = "Sessão de treino completa de um atleta")
public record WorkoutSession(

        @Schema(description = "ID único do atleta", example = "atleta-001")
        @NotBlank(message = "O ID do atleta é obrigatório")
        String atletaId,

        @Schema(description = "Data da sessão de treino", example = "2026-05-29")
        @NotNull(message = "A data da sessão é obrigatória")
        @PastOrPresent(message = "A sessão não pode ser no futuro")
        LocalDate data,

        @Schema(description = "Lista de séries executadas na sessão")
        @NotNull(message = "A lista de sets é obrigatória")
        @Size(min = 1, message = "A sessão deve ter ao menos 1 set")
        @Valid
        List<ExerciseSet> sets,

        @Schema(description = "RPE — Rate of Perceived Exertion: esforço percebido de 1 (leve) a 10 (máximo)", example = "8")
        @NotNull(message = "O RPE é obrigatório")
        @Min(value = 1, message = "RPE mínimo é 1")
        @Max(value = 10, message = "RPE máximo é 10")
        Integer rpe

) {}
