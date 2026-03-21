package com.learn.bookService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.learn.bookService.client.PricingClient;

/**
 * JUnit starts
↓
SpringExtension hooks in
↓
Spring Boot context loads
↓
Beans initialized
↓
Test passes if startup succeeds

 */

/** @ExtendWith(SpringExtension.class)
 * “Run this test with Spring support.”
 * It enables:

✅ Dependency injection
✅ Spring lifecycle management
✅ Application context loading
✅ Test annotations
 */

/** @SpringBootTest
 * Start the entire application context like a real app.”
 * Spring will:

✔ Boot auto-configuration
✔ Load beans
✔ Scan components
✔ Setup environment

Basically:

Simulates real application startup
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
	    "spring.cloud.consul.enabled=false",
	    "spring.cloud.consul.discovery.enabled=false",
	    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999",
	    "auth.server.token-uri=http://localhost:9999/oauth2/token",
	    "auth.server.client-id=test-client",
	    "auth.server.client-secret=test-secret",
	    "management.tracing.sampling.probability=0.0",
	    "management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans"
	})
class ApplicationTests{// extends BookServiceApplicationTests {

	/*
	 * If something is misconfigured:
	missing bean
	circular dependency
	bad config
	👉 this test will fail immediately.
	 */
	
	@MockBean
    private PricingClient pricingClient;  // Mock it so URL is never needed
	@Test
	void contextLoads() {
	}

}
