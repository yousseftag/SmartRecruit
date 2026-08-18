package com.smartrecruit.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "SmartRecruit API", version = "v1"),
    security = {@SecurityRequirement(name = "keycloak"), @SecurityRequirement(name = "bearerAuth")})
@SecuritySchemes({
  @SecurityScheme(
      name = "keycloak",
      type = SecuritySchemeType.OAUTH2,
      flows =
          @OAuthFlows(
              password =
                  @OAuthFlow(
                      tokenUrl =
                          "http://localhost:8081/realms/smartrecruit/protocol/openid-connect/token"))),
  @SecurityScheme(
      name = "bearerAuth",
      type = SecuritySchemeType.HTTP,
      scheme = "bearer",
      bearerFormat = "JWT")
})
public class OpenApiConfig {}
