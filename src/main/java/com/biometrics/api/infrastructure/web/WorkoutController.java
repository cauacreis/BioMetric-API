package com.biometrics.api.infrastructure.web;

import com.biometrics.api.application.dto.MetricsResponseDTO;
import com.biometrics.api.domain.model.WorkoutSession;
import com.biometrics.api.domain.service.VolumeCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de Infraestrutura — WorkoutController.
 *
 * Pertence à camada de Infrastructure/Web: recebe requisições HTTP,
 * delega ao domínio e devolve respostas ao mundo externo.
 * O Controller NÃO contém lógica de negócio — apenas orquestra chamadas.
 */
@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "Análise de métricas de volume e fadiga de sessões de treino")
public class WorkoutController {

    private final VolumeCalculatorService volumeCalculatorService;

    public WorkoutController(VolumeCalculatorService volumeCalculatorService) {
        this.volumeCalculatorService = volumeCalculatorService;
    }

    /**
     * POST /api/metrics/analyze
     *
     * Recebe o JSON de uma sessão de treino, calcula a tonelagem total
     * e retorna as métricas com a mensagem do Orquestrador LLM (mock).
     */
    @PostMapping("/analyze")
    @Operation(
        summary = "Analisar sessão de treino",
        description = """
                Recebe uma sessão de treino completa com todos os sets executados.
                Calcula a **Tonelagem Total** (Σ carga × repetições) e a **Tonelagem Ajustada pelo RPE**.
                Retorna as métricas e a mensagem do Orquestrador LLM aguardando decisão de Deload.
                """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Sessão de treino do atleta",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkoutSession.class),
                examples = @ExampleObject(
                    name = "Treino de Peito/Ombro",
                    summary = "Exemplo: treino de hipertrofia com RPE 8",
                    value = """
                            {
                              "atletaId": "atleta-001",
                              "data": "2026-05-29",
                              "rpe": 8,
                              "sets": [
                                { "nomeExercicio": "Supino Reto com Barra",  "carga": 100.0, "repeticoes": 8, "rir": 2 },
                                { "nomeExercicio": "Supino Reto com Barra",  "carga": 100.0, "repeticoes": 7, "rir": 1 },
                                { "nomeExercicio": "Supino Reto com Barra",  "carga": 100.0, "repeticoes": 6, "rir": 0 },
                                { "nomeExercicio": "Crucifixo Inclinado",    "carga": 22.0,  "repeticoes": 12, "rir": 3 },
                                { "nomeExercicio": "Crucifixo Inclinado",    "carga": 22.0,  "repeticoes": 11, "rir": 2 },
                                { "nomeExercicio": "Desenvolvimento Militar","carga": 60.0,  "repeticoes": 10, "rir": 2 },
                                { "nomeExercicio": "Desenvolvimento Militar","carga": 60.0,  "repeticoes": 9,  "rir": 1 },
                                { "nomeExercicio": "Elevação Lateral",       "carga": 14.0,  "repeticoes": 15, "rir": 3 }
                              ]
                            }
                            """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Métricas calculadas com sucesso",
                content = @Content(schema = @Schema(implementation = MetricsResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Payload inválido — verifique os campos obrigatórios")
        }
    )
    public ResponseEntity<MetricsResponseDTO> analisarSessao(
            @Valid @RequestBody WorkoutSession session) {

        double tonelagemTotal      = volumeCalculatorService.calcularTonelagemTotal(session);
        double tonelagemAjustada   = volumeCalculatorService.calcularTonelagemAjustadaPorRpe(session);

        MetricsResponseDTO response = new MetricsResponseDTO(
                session.atletaId(),
                session.data().toString(),
                tonelagemTotal,
                tonelagemAjustada,
                session.rpe(),
                session.sets().size(),
                MetricsResponseDTO.MSG_AGUARDANDO_LLM
        );

        return ResponseEntity.ok(response);
    }
}
