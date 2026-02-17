package com.learn.bookService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
class ApplicationTests {

	/*
	 * If something is misconfigured:
	missing bean
	circular dependency
	bad config
	👉 this test will fail immediately.
	 */
	@Test
	void contextLoads() {
	}

}
