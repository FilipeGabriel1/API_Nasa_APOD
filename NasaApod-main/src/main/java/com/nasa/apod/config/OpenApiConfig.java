package com.nasa.apod.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nasa APOD API")
                        .version("1.0")
                        .description("API que busca APOD (Astronomy Picture Of the Day) da NASA e traduz título/explicação para pt-BR."));
    }
}
