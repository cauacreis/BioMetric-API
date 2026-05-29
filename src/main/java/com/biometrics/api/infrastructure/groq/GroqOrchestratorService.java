package com.biometrics.api.infrastructure.groq;

import com.biometrics.api.application.dto.LlmDecisionDTO;
import com.biometrics.api.domain.model.WorkoutSession;
import com.biometrics.api.infrastructure.groq.dto.GroqRequest;
import com.biometrics.api.infrastructure.groq.dto.GroqResponse;
import com.biometrics.api.infrastructure.groq.exception.GroqIntegrationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Serviço de Infraestrutura — GroqOrchestratorService.
 *
 * Orquestra a comunicação com a API do Groq (OpenAI-compatible).
 * Responsabilidade: receber os dados biomecânicos da sessão, montar o prompt
 * estruturado e retornar a decisão do Treinador Virtual como LlmDecisionDTO.
 *
 * Fluxo:
 *  WorkoutSession + métricas calculadas
 *    → System Prompt (regras de negócio)
 *    → User Message (dados da sessão)
 *    → POST https://api.groq.com/openai/v1/chat/completions
 *    → JSON da LLM
 *    → LlmDecisionDTO
 */
@Service
public class GroqOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(GroqOrchestratorService.class);

    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    /**
     * System Prompt do Treinador Virtual.
     * Define o contexto, as regras de negócio e o formato obrigatório de resposta.
     */
    private static final String SYSTEM_PROMPT = """
            Você é um treinador de hipertrofia de elite com profundo conhecimento em \
            ciência do esporte e biomecânica. Analise os dados de Tonelagem e RPE da \
            sessão de treino fornecida pelo atleta.
            
            Regras de decisão:
            - Se o RPE for maior ou igual a 8 E a tonelagem estiver acima de 3000kg: \
            recomende 'DELOAD' com justificativa de fadiga acumulada.
            - Se o RPE for maior ou igual a 8 E a tonelagem entre 1500kg e 3000kg: \
            recomende 'REDUÇÃO DE VOLUME' (15-20% menos sets na próxima semana).
            - Se o RPE estiver entre 6 e 7: recomende 'MANTER VOLUME' e sugira \
            ajuste de intensidade.
            - Se o RPE for menor que 6: recomende 'AUMENTAR VOLUME' com progressão.
            
            IMPORTANTE: Responda OBRIGATORIAMENTE em formato JSON puro, sem markdown, \
            sem texto adicional, apenas o objeto JSON com exatamente dois campos:
            {
              "decisao": "<String curta com a decisão>",
              "justificativaBiomecanica": "<String explicativa com a justificativa técnica>"
            }
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String groqModel;

    public GroqOrchestratorService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Analisa a sessão de treino e retorna a decisão do Treinador Virtual.
     *
     * @param session          Aggregate Root da sessão de treino
     * @param tonelagemTotal   Tonelagem total já calculada pelo VolumeCalculatorService
     * @param tonelagemAjustada Tonelagem ajustada pelo RPE
     * @return Decisão estruturada da LLM ou LlmDecisionDTO de erro em caso de falha
     */
    public LlmDecisionDTO analisarSessao(
            WorkoutSession session,
            double tonelagemTotal,
            double tonelagemAjustada) {

        String userMessage = montarUserMessage(session, tonelagemTotal, tonelagemAjustada);
        log.debug("Enviando para Groq [atleta={}]: {}", session.atletaId(), userMessage);

        try {
            GroqResponse groqResponse = chamarApiGroq(userMessage);
            String jsonContent = groqResponse.extrairConteudo();
            log.debug("Groq respondeu: {}", jsonContent);
            return parsearRespostaLlm(jsonContent);

        } catch (GroqIntegrationException e) {
            // Re-lança exceções de integração para tratamento no GlobalExceptionHandler
            throw e;

        } catch (Exception e) {
            log.error("Erro inesperado ao chamar Groq: {}", e.getMessage(), e);
            return LlmDecisionDTO.deErro("Erro inesperado: " + e.getMessage());
        }
    }

    // =========================================================================
    //  Métodos privados auxiliares
    // =========================================================================

    /**
     * Monta a User Message com os dados da sessão em formato estruturado.
     * Este texto é o que a LLM recebe como entrada para análise.
     */
    private String montarUserMessage(
            WorkoutSession session,
            double tonelagemTotal,
            double tonelagemAjustada) {

        return String.format("""
                Analise a sessão de treino abaixo e tome uma decisão de periodização:
                
                - Atleta ID:             %s
                - Data da Sessão:        %s
                - RPE da Sessão:         %d/10
                - Total de Sets:         %d
                - Tonelagem Total:       %.2f kg
                - Tonelagem Ajustada:    %.2f kg
                
                Tome sua decisão baseada nas regras de negócio do seu sistema prompt.
                """,
                session.atletaId(),
                session.data(),
                session.rpe(),
                session.sets().size(),
                tonelagemTotal,
                tonelagemAjustada
        );
    }

    /**
     * Executa a chamada HTTP POST para a API do Groq.
     * Trata os possíveis erros HTTP com mensagens específicas por status code.
     */
    private GroqResponse chamarApiGroq(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        GroqRequest requestBody = GroqRequest.ofTreinador(SYSTEM_PROMPT, userMessage, groqModel);
        HttpEntity<GroqRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GroqResponse> response = restTemplate.exchange(
                    GROQ_API_URL,
                    HttpMethod.POST,
                    request,
                    GroqResponse.class
            );

            if (response.getBody() == null) {
                throw new GroqIntegrationException(
                        "Groq retornou corpo vazio.", HttpStatus.BAD_GATEWAY);
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            String mensagem = switch (e.getStatusCode().value()) {
                case 401 -> "Chave de API Groq inválida ou expirada. Verifique groq.api.key.";
                case 429 -> "Rate limit da API Groq excedido. Aguarde antes de tentar novamente.";
                case 400 -> "Payload inválido enviado para Groq: " + e.getResponseBodyAsString();
                default  -> "Erro do cliente Groq [" + e.getStatusCode() + "]: " + e.getMessage();
            };
            log.error("Groq 4xx: {}", mensagem);
            throw new GroqIntegrationException(mensagem, e, HttpStatus.BAD_GATEWAY);

        } catch (HttpServerErrorException e) {
            String mensagem = "Groq indisponível [" + e.getStatusCode() + "]. Tente novamente.";
            log.error("Groq 5xx: {}", mensagem);
            throw new GroqIntegrationException(mensagem, e, HttpStatus.SERVICE_UNAVAILABLE);

        } catch (ResourceAccessException e) {
            String mensagem = "Timeout ou falha de rede ao conectar ao Groq: " + e.getMessage();
            log.error("Groq timeout: {}", mensagem);
            throw new GroqIntegrationException(mensagem, e, HttpStatus.GATEWAY_TIMEOUT);
        }
    }

    /**
     * Parseia o JSON retornado pela LLM para o DTO tipado LlmDecisionDTO.
     * A LLM é instruída a retornar JSON puro; se falhar, loga e retorna erro.
     */
    private LlmDecisionDTO parsearRespostaLlm(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, LlmDecisionDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("LLM retornou JSON inválido: {}. Conteúdo: {}", e.getMessage(), jsonContent);
            return LlmDecisionDTO.deErro(
                    "LLM retornou formato inesperado. Conteúdo bruto: " + jsonContent);
        }
    }
}
