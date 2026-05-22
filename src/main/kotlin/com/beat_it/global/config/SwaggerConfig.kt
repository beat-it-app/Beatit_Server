package com.beat_it.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val jwtSchemeName = "JWT AUTH"

        val securityRequirement = SecurityRequirement().addList(jwtSchemeName)

        val components = Components()
            .addSecuritySchemes(
                jwtSchemeName, SecurityScheme()
                    .name(jwtSchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )

        return OpenAPI()
            .info(
                Info()
                    .title("Beat It API 명세서")
                    .description("Beat It 프로젝트의 API 문서입니다.")
                    .version("1.0.0")
            )
            .addSecurityItem(securityRequirement)
            .components(components)
    }
}