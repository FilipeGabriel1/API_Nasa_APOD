package com.nasa.apod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class NasaApodApplication {

	public static void main(String[] args) {
		SpringApplication.run(NasaApodApplication.class, args);
	}
	// Método principal para iniciar a aplicação Spring Boot
	// O Spring cuidará de criar o contexto e injetar beans

	/**
	 * Bean RestTemplate: disponibiliza um cliente HTTP reutilizável para toda a aplicação.
	 * - É injetado em `NasaController` e `TraducaoService`.
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
}
