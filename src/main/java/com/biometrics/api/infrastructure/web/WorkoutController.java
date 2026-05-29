package com.biometrics.api.infrastructure.web;

import com.biometrics.api.application.dto.LlmDecisionDTO;
import com.biometrics.api.application.dto.MetricsResponseDTO;
import com.biometrics.api.domain.model.WorkoutSession;
import com.biometrics.api.domain.service.VolumeCalculatorService;
import com.biometrics.api.infrastructure.groq.GroqOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de Infraestrutura — WorkoutController.
 *
 * Orquestra o pipeline completo de análise:
 *  1. Recebe WorkoutSession via POST
 *  2. Calcula métricas com VolumeCalculatorService (domínio)
 *  3. Envia métricas ao GroqOrchestratorService (infra → LLM)
 *  4. Retorna MetricsResponseDTO com decisão do Treinador Virtual
 */
@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "Análise biomecânica de sessões de treino com decisão do Treinador Virtual (LLM)")
public class WorkoutController {

    private static final Logger log = LoggerFactory.getLogger(WorkoutController.class);

    private final VolumeCalculatorService volumeCalculatorService;
    private final GroqOrchestratorService groqOrchestratorService;

    public WorkoutController(
            VolumeCalculatorService volumeCalculatorService,
            GroqOrchestratorService groqOrchestratorService) {
        this.volumeCalculatorService = volumeCalculatorService;
        this.groqOrchestratorService = groqOrchestratorService;
    }

    /**
     * POST /api/metrics/analyze
     *
     * Pipeline:
     *   JSON → WorkoutSession → Tonelagem (domínio) → LLM Groq → MetricsResponseDTO
     */
    @PostMapping("/analyze")
    @Operation(
        summary = "Analisar sessão com Treinador Virtual",
        description = """
                Recebe uma sessão de treino completa com todos os sets executados.
                
                **Pipeline de análise:**
                1. Calcula a **Tonelagem Total** (Σ carga × repetições)
                2. Calcula a **Tonelagem Ajustada** pelo RPE (fadiga real estimada)
                3. Envia os dados ao **Treinador Virtual** (LLM Groq - llama3-8b-8192)
                4. Retorna a **decisão de periodização** com justificativa biomecânica
                
                **Possíveis decisões da IA:** DELOAD | REDUÇÃO DE VOLUME | MANTER VOLUME | AUMENTAR VOLUME
                """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Sessão de treino do atleta",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WorkoutSession.class),
                examples = @ExampleObject(
                    name = "Treino de Peito/Ombro — RPE Alto",
                    summary = "Sessão com RPE 9 → espera-se decisão de DELOAD",
                    value = """
                            {
                              "atletaId": "atleta-001",
                              "data": "2026-05-29",
                              "rpe": 9,
                              "sets": [
                                { "nomeExercicio": "Supino Reto com Barra",   "carga": 105.0, "repeticoes": 6, "rir": 0 },
                                { "nomeExercicio": "Supino Reto com Barra",   "carga": 105.0, "repeticoes": 5, "rir": 0 },
                                { "nomeExercicio": "Supino Reto com Barra",   "carga": 100.0, "repeticoes": 5, "rir": 1 },
                                { "nomeExercicio": "Crucifixo Inclinado",     "carga": 24.0,  "repeticoes": 12, "rir": 1 },
                                { "nomeExercicio": "Crucifixo Inclinado",     "carga": 24.0,  "repeticoes": 10, "rir": 0 },
                                { "nomeExercicio": "Desenvolvimento Militar", "carga": 65.0,  "repeticoes": 8,  "rir": 1 },
                                { "nomeExercicio": "Desenvolvimento Militar", "carga": 65.0,  "repeticoes": 7,  "rir": 0 },
                                { "nomeExercicio": "Elevação Lateral",        "carga": 16.0,  "repeticoes": 15, "rir": 2 }
                              ]
                            }
                            """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Análise concluída — decisão do Treinador Virtual incluída",
                content = @Content(schema = @Schema(implementation = MetricsResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400",  description = "Payload inválido — verifique os campos obrigatórios"),
            @ApiResponse(responseCode = "502",  description = "Falha na integração com o Groq (API key, payload)"),
            @ApiResponse(responseCode = "503",  description = "Groq indisponível"),
            @ApiResponse(responseCode = "504",  description = "Timeout na chamada ao Groq")
        }
    )
    public ResponseEntity<MetricsResponseDTO> analisarSessao(
            @Valid @RequestBody WorkoutSession session) {

        log.info("Analisando sessão: atleta={}, rpe={}, sets={}",
                session.atletaId(), session.rpe(), session.sets().size());

        // === STEP 1: Matemática de domínio ===
        double tonelagemTotal    = volumeCalculatorService.calcularTonelagemTotal(session);
        double tonelagemAjustada = volumeCalculatorService.calcularTonelagemAjustadaPorRpe(session);

        log.debug("Tonelagem calculada: total={}kg, ajustada={}kg", tonelagemTotal, tonelagemAjustada);

        // === STEP 2: Orquestração LLM — Treinador Virtual ===
        LlmDecisionDTO decisao = groqOrchestratorService.analisarSessao(
                session, tonelagemTotal, tonelagemAjustada);

        log.info("Decisão do Treinador Virtual: {} | atleta={}", decisao.decisao(), session.atletaId());

        // === STEP 3: Montar e retornar resposta ===
        MetricsResponseDTO response = new MetricsResponseDTO(
                session.atletaId(),
                session.data().toString(),
                tonelagemTotal,
                tonelagemAjustada,
                session.rpe(),
                session.sets().size(),
                decisao
        );

        return ResponseEntity.ok(response);
    }
}
