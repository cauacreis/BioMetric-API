package com.biometrics.api.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuração de Infraestrutura — AppConfig.
 * Define beans de infraestrutura reutilizáveis: RestTemplate com timeouts configurados.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate com timeouts explícitos para evitar que chamadas
     * à API do Groq (ou qualquer LLM externo) bloqueiem threads indefinidamente.
     *
     * - connectTimeout: tempo máximo para estabelecer a conexão TCP
     * - readTimeout: tempo máximo esperando a resposta do servidor
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);   // 5 segundos para conectar
        factory.setReadTimeout(30_000);     // 30 segundos para LLM responder
        return new RestTemplate(factory);
    }
}
