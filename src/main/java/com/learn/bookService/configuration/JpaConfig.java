package com.learn.bookService.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * This configuration class is responsible for setting up JPA repositories and entity scanning for the application.
 * By placing this configuration in a separate package (com.example.demo.configuration), we ensure that it is not automatically picked up by Spring Boot when it scans for components in the main application package (com.example.demo).
 * This allows us to control when and where this configuration is applied, preventing it from affecting tests or other contexts that do not require JPA repositories.
 * In this case, we are enabling JPA repositories in the "com.example.demo.persistence.repo" package and scanning for JPA entities in the "com.example.demo.persistence.model" package.
 * This separation of concerns helps to keep the application context clean and focused on the specific configurations needed for different parts of the application.
 * 
 */
@Configuration
@EnableJpaRepositories(
    value = "com.example.demo.persistence.repo"
)
@EntityScan("com.example.demo.persistence.model")
public class JpaConfig {
}
