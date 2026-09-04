package com.codexa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI codexaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Codexa API")
                        .description("AI-assisted code review, security auditing, and production-readiness platform API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Codexa Engineering Team")
                                .url("https://github.com/Codewithjainam7/Codexa"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
