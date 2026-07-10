package com.delivery.global.config;

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
                .components(
                        new Components().addSecuritySchemes("bearerAuth", createSecurityScheme()))
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

                        - 안예지 팀장 : 주문
                        - 이용현 팀원 : 리뷰
                        - 정수민 팀원 : 음식점
                        - 임은택 팀원 : 메뉴
                        - 송채영 팀원 : 결제
                        - 이강석 팀원 : 사용자

                        ## 🔐 Authentication

                        Request Header:

                        ```
                        Authorization: Bearer {accessToken}
                        ```

                        ## 📌 Response Format

                        성공 응답 예시

                        ```
                        {
                          "success": true,
                          "code": 200,
                          "message": "조회 성공",
                          "data": {...},
                          "error": null
                        }
                        ```

                        ## 🛠 Tech Stack
                        ### BACKEND
                        - Java 17
                        - Spring Boot 3.5.16
                        - Spring DATA JPA

                        ### DATABASE
                        - PostgreSQL 17
                        """)
                .version("v1.0.0")
                .contact(new Contact().name("코딩의 민족"));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
