package com.fsocial.postservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api/v1/post}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Local Development Server");

        Server gatewayServer = new Server()
                .url("http://localhost:8888/api/v1/post")
                .description("API Gateway Server");

        return new OpenAPI()
                .info(new Info()
                        .title("Post Service API")
                        .version("1.0.0")
                        .description("API documentation cho Post Service — quản lý bài viết, bình luận, tài khoản, xác thực")
                        .license(new License()
                                .name("FSocial")
                                .url("https://www.fsocial.com")))
                .servers(List.of(localServer, gatewayServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập JWT access token (không cần tiền tố Bearer)")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }
}
