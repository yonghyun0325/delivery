package com.delivery.global.config;

import com.delivery.global.security.jwt.JwtHeaderType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger Config */
@Configuration
public class ApiDocumentationConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8080")
                                        .description("Local Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("refreshToken"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", createSecurityScheme("bearer", "JWT"))
                        .addSecuritySchemes("refreshToken", createRefreshTokenScheme()))
                .externalDocs(
                        new ExternalDocumentation()
                                .description("Delivery Git Repository")
                                .url("https://github.com/yonghyun0325/delivery"));
    }

    private Info apiInfo() {
        return new Info()
                .title("🍽️ Delivery API")
                .description(
                        """
                        백엔드 REST API 문서입니다.

                        ## 🚀 팀원 및 역할분담

                        - 안예지 팀장 : 주문(Order)
                        - 이용현 팀원 : 리뷰(Review)
                        - 정수민 팀원 : 가게(Store)
                        - 임은택 팀원 : 메뉴(Menu)
                        - 송채영 팀원 : 결제(Payment)
                        - 이강석 팀원 : 회원(User)

                        ## 🛠 기술 스택
                        ### BACKEND
                        - Java 17
                        - Spring Boot 3.5.16
                        - Spring DATA JPA

                        ### DATABASE
                        - PostgreSQL 17

                        ## DevOps

                        * Docker
                        * Docker Compose

                        ## Build Tool

                        * Gradle
                        """)
                .contact(new Contact().name("코딩의 민족"));
    }

    private SecurityScheme createSecurityScheme(String scheme, String format) {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

    private SecurityScheme createRefreshTokenScheme() {
        return new SecurityScheme()
                .name("Refresh Token")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(JwtHeaderType.REFRESH_TOKEN.getHeader());
    }
}
