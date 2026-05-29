package com.biometrics.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da BioMetrics API.
 * Motor de Periodização de Hipertrofia — Arquitetura DDD.
 */
@SpringBootApplication
public class BiometricsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiometricsApiApplication.class, args);
    }
}
