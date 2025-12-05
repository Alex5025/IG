package org.inariforge.ig.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// 僅在非 prod profile 時啟用 OpenAPI/Swagger 設定
@Profile("!prod")
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ig API")
                        .version("0.0.1")
                        .description("Ig - API documentation")
                        .contact(new Contact().name("InariForge Team").email("team@inariforge.org"))
                );
    }
}
