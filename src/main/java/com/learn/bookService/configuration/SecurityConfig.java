package com.learn.bookService.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This configuration class sets up Spring Security for the application. It defines a security filter chain that specifies which endpoints are publicly accessible and which require authentication.
 * The filterChain() method configures the security settings:
 * - It allows unauthenticated access to specific endpoints such as "/actuator/prometheus", "/actuator/health", "/actuator/info", "/api/**", "/swagger-ui/**", and "/v3/api-docs/**". This is important for allowing monitoring tools like Prometheus to access the metrics endpoint and for allowing access to API documentation without requiring authentication.
 * - It requires authentication for any other requests that are not explicitly permitted.
 * - It disables CSRF protection, which is often necessary for APIs that are accessed by non-browser clients.
 * - It enables form-based login with a default success URL of "/home", which means that after a successful login, users will be redirected to the "/home" page.
 * - It also enables HTTP Basic authentication, allowing clients to authenticate using basic credentials.
 * This configuration ensures that sensitive endpoints are protected while allowing necessary access for monitoring and documentation purposes.
 * 
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(expressionInterceptUrlRegistry ->
            expressionInterceptUrlRegistry.requestMatchers(
                    "/actuator/prometheus",
                    "/actuator/health",
                    "/actuator/info",
                    "/api/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()      // ✅ allow Prometheus & health checks
                
//              .permitAll())  // allows anyone to access them without authentication
            	.anyRequest().authenticated())  // 🔒 everything else requires login
          .csrf(AbstractHttpConfigurer::disable)
          .formLogin(form -> form
        	        .defaultSuccessUrl("/home", true)   // ⭐ force redirect to /home
        	        .permitAll()
        		  )           // optional: enables default login page
          .httpBasic(Customizer.withDefaults());          // optional: enables HTTP Basic Auth;
        return http.build();
    }
}


