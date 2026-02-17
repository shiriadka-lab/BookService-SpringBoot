package com.learn.bookService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Scan this package for Spring Data JPA repository interfaces
//@EnableJpaRepositories("com.example.demo.persistence.repo") 
// Scan this package for JPA entities (@Entity)
//@EntityScan("com.example.demo.persistence.model")

/**
 *  Do not have any other configuration classes in the same package as the main application class.
 *  Otherwise, Spring Boot will pick them up and apply them to the entire application context.
 *  This can lead to unintended consequences, such as:
 *  - Beans being created that are not needed for the main application
 *  - Configuration being applied to tests that should be isolated
 *  - Longer startup times due to unnecessary bean initialization
 *  To avoid this, keep the main application class in a separate package (e.g., com.example.demo) and place other configuration classes in subpackages (e.g., com.example.demo.configuration).
 *  This way, you can control which configurations are applied to the main application and which are
 *  applied to tests or other contexts.
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
