package com.biometrics.api.domain.service;

import com.biometrics.api.domain.model.ExerciseSet;
import com.biometrics.api.domain.model.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Testes unitários do VolumeCalculatorService.
 * Domínio puro — sem contexto Spring, sem banco, roda em milissegundos.
 */
@DisplayName("VolumeCalculatorService — Cálculo de Tonelagem")
class VolumeCalculatorServiceTest {

    private VolumeCalculatorService service;

    @BeforeEach
    void setUp() {
        service = new VolumeCalculatorService();
    }

    @Test
    @DisplayName("Deve calcular tonelagem total corretamente para múltiplos sets")
    void devecalcularTonelagemTotalCorretamente() {
        // Arrange
        // Set 1: 100kg × 8 reps = 800kg
        // Set 2: 100kg × 7 reps = 700kg
        // Set 3:  22kg × 12 reps = 264kg
        // Total esperado = 1764kg
        var sets = List.of(
                new ExerciseSet("Supino Reto", 100.0, 8, 2),
                new ExerciseSet("Supino Reto", 100.0, 7, 1),
                new ExerciseSet("Crucifixo",    22.0, 12, 3)
        );
        var session = new WorkoutSession("atleta-001", LocalDate.now(), sets, 8);

        // Act
        Double tonelagem = service.calcularTonelagemTotal(session);

        // Assert
        assertThat(tonelagem).isEqualTo(1764.0);
    }

    @Test
    @DisplayName("Deve retornar zero para sets com carga zero")
    void deveRetornarZeroParaCargaZero() {
        var sets = List.of(new ExerciseSet("Peso Corporal", 0.0, 10, 1));
        var session = new WorkoutSession("atleta-002", LocalDate.now(), sets, 5);

        assertThat(service.calcularTonelagemTotal(session)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve calcular tonelagem ajustada por RPE corretamente")
    void deveCalcularTonelagemAjustadaPorRpe() {
        // Arrange: 100kg × 10 reps = 1000kg; RPE=8 → 1000 × 0.8 = 800
        var sets = List.of(new ExerciseSet("Agachamento", 100.0, 10, 2));
        var session = new WorkoutSession("atleta-003", LocalDate.now(), sets, 8);

        // Act
        Double tonelagemAjustada = service.calcularTonelagemAjustadaPorRpe(session);

        // Assert
        assertThat(tonelagemAjustada).isCloseTo(800.0, within(0.01));
    }

    @Test
    @DisplayName("RPE máximo (10) deve resultar em tonelagem ajustada igual à total")
    void rpeMaximoDeveResultarEmTonelagemTotal() {
        var sets = List.of(new ExerciseSet("Levantamento Terra", 200.0, 5, 0));
        var session = new WorkoutSession("atleta-004", LocalDate.now(), sets, 10);

        double total    = service.calcularTonelagemTotal(session);
        double ajustada = service.calcularTonelagemAjustadaPorRpe(session);

        // RPE=10 → multiplicador 1.0 → ajustada == total
        assertThat(ajustada).isEqualTo(total);
    }
}
