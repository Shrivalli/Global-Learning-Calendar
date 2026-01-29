package com.learning.globallearningcalendar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Global Learning Calendar API")
                        .version("1.0.0")
                        .description("REST API for the Global Learning Calendar Application. " +
                                "This API enables employees to discover, book, and manage learning slots, " +
                                "managers to track participation and learning trends, and L&D admins to " +
                                "create, manage, and report on learning programs.")
                        .contact(new Contact()
                                .name("L&D Team")
                                .email("ld-support@company.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.learning-calendar.com").description("Production Server")
                ));
    }
}
