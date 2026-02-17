package com.learn.bookService.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * This configuration class sets up Swagger (OpenAPI) for the application. 
 * It defines the OpenAPI specification for the API, including metadata such as the 
 * title, version, description, and contact information. It also configures a security scheme
 * for basic authentication, allowing users to authenticate when accessing the API documentation.
 * The customOpenAPI() method creates an OpenAPI bean that provides metadata about the API, 
 * which will be displayed in the Swagger UI. The publicApi() method defines a GroupedOpenApi
 * bean that specifies which endpoints should be included in the API documentation 
 * (in this case, all endpoints under "/api/**"). The secureOpenAPI() method sets up
 *  a security scheme for basic authentication, which adds an "Authorize" button to the Swagger UI,
 *   allowing users to enter their credentials to access protected endpoints.
 * 
 */
@Configuration
public class SwaggerConfig {

	// Nice header + metadata in Swagger.
	/**
	 * Defines the OpenAPI specification for the API, including metadata such as
	 *  title, version, description, and contact information.
	 * This information will be displayed in the Swagger UI, providing users with details
	 *  about the API and how to contact the development team if needed.
	 * The customOpenAPI() method creates an OpenAPI bean that contains this metadata,
	 *  which is essential for generating comprehensive API documentation.
	 * 
	 * @return
	 */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Book API")
                .version("1.0")
                .description("API for managing books")
                .contact(new Contact()
                    .name("Book Service Team")
                    .email("team@example.com")));
    }
    
    /**
     * Defines the public API group for Swagger documentation.
     * @return
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/**")
            .build();
    }
    
    /**
     * Configures a security scheme for basic authentication in the OpenAPI specification.
     * This method adds a security scheme named "basicAuth" of type HTTP with the scheme "basic".
     * It also adds a security requirement that references this scheme, which will prompt users
     * to authenticate when accessing protected endpoints in the Swagger UI. This allows users
     * to enter their credentials using the "Authorize" button in the Swagger UI, enabling
     * them to access endpoints that require authentication.
     * 
     * @return
     */
    // 👉 🔒 Authorize button
    public OpenAPI secureOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("basicAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}



