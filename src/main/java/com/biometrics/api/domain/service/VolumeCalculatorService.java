package com.biometrics.api.domain.service;

import com.biometrics.api.domain.model.WorkoutSession;
import org.springframework.stereotype.Service;

/**
 * Domain Service — VolumeCalculatorService.
 *
 * Responsabilidade única: calcular métricas de volume de uma sessão de treino.
 * Pertence à camada de Domínio pois encapsula regras de negócio puras
 * (matemática esportiva) que não pertencem a nenhuma entidade isolada.
 *
 * Fórmulas implementadas:
 *  - Tonelagem Total  = Σ (carga × repetições) de todos os sets
 *  - Tonelagem Ponderada (futura) = Tonelagem × (1 + RPE / 100)
 */
@Service
public class VolumeCalculatorService {

    /**
     * Calcula a Tonelagem Total da sessão.
     *
     * A tonelagem é a métrica primária de volume de treinamento:
     * quanto maior a tonelagem, maior o estímulo mecânico acumulado.
     *
     * @param session A sessão de treino do atleta (Aggregate Root)
     * @return Tonelagem total em kg (carga × reps, somada de todos os sets)
     */
    public Double calcularTonelagemTotal(WorkoutSession session) {
        return session.sets()
                .stream()
                .mapToDouble(set -> set.carga() * set.repeticoes())
                .sum();
    }

    /**
     * Calcula a Tonelagem Ajustada pelo RPE da sessão.
     *
     * Aplica um multiplicador baseado no esforço percebido para refletir
     * a fadiga real gerada — base para a futura decisão de Deload pelo LLM.
     *
     * Fórmula: TonelagemTotal × (RPE / 10)
     * Quanto mais alto o RPE, maior o custo de recuperação estimado.
     *
     * @param session A sessão de treino do atleta
     * @return Tonelagem ajustada pela intensidade percebida
     */
    public Double calcularTonelagemAjustadaPorRpe(WorkoutSession session) {
        double tonelagemTotal = calcularTonelagemTotal(session);
        return tonelagemTotal * (session.rpe() / 10.0);
    }
}
