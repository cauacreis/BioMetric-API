package com.biometrics.api.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Entidade de Domínio Pura — ExerciseSet.
 *
 * Representa um set (série) de um exercício dentro de uma sessão de treino.
 * Deliberadamente sem anotações JPA: o domínio não conhece a infraestrutura.
 *
 * Tonelagem do set = carga * repeticoes
 */
@Schema(description = "Série de um exercício com carga, repetições e RIR")
public record ExerciseSet(

        @Schema(description = "Nome do exercício", example = "Supino Reto com Barra")
        @NotBlank(message = "O nome do exercício é obrigatório")
        String nomeExercicio,

        @Schema(description = "Carga utilizada em kg", example = "100.0")
        @NotNull(message = "A carga é obrigatória")
        @PositiveOrZero(message = "A carga não pode ser negativa")
        Double carga,

        @Schema(description = "Número de repetições executadas", example = "8")
        @NotNull(message = "As repetições são obrigatórias")
        @Min(value = 1, message = "Deve haver ao menos 1 repetição")
        Integer repeticoes,

        @Schema(description = "RIR — Reps In Reserve: quantas reps sobraram no tanque (0 = falha)", example = "2")
        @NotNull(message = "O RIR é obrigatório")
        @Min(value = 0, message = "RIR não pode ser negativo")
        Integer rir

) {
    /**
     * Calcula a tonelagem bruta deste set específico.
     * Método de domínio: a lógica pertence à entidade.
     */
    public Double calcularTonelagemDoSet() {
        return carga * repeticoes;
    }
}
