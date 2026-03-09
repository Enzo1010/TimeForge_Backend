package br.com.timeforge.timeforge_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TimeForge API",
                version = "v1",
                description = "Contrato v1 congelado em 2026-03-09. Endpoints de atualizacao usam PUT."
        )
)
public class OpenApiConfig {
}
