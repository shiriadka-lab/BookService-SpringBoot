package com.learn.bookService.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.learn.bookService.persistence.service.CustomUserDetailsService;

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

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//          .authorizeHttpRequests(expressionInterceptUrlRegistry ->
//            expressionInterceptUrlRegistry.requestMatchers(
//                    "/actuator/prometheus",
//                    "/actuator/health",
//                    "/actuator/info",
//                    "/api/**",
//                    "/swagger-ui/**",
//                    "/v3/api-docs/**"
//                ).permitAll()      // ✅ allow Prometheus & health checks
//                
////              .permitAll())  // allows anyone to access them without authentication
//            	.anyRequest().authenticated())  // 🔒 everything else requires login
//          .csrf(AbstractHttpConfigurer::disable)
//          .formLogin(form -> form
//        	        .defaultSuccessUrl("/home", true)   // ⭐ force redirect to /home
//        	        .permitAll()
//        		  )           // optional: enables default login page
//          .httpBasic(Customizer.withDefaults());          // optional: enables HTTP Basic Auth;
//        return http.build();
//    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Autowired
    private CustomUserDetailsService userDetailsService; // your DB-based service

    @Autowired
    private JwtFilter jwtFilter; // your JWT filter

    // Expose AuthenticationManager as a bean so it can be autowired in AuthController
    // This method retrieves the AuthenticationManager from the AuthenticationConfiguration, 
    // which is automatically configured by Spring Security based on the authentication providers
    // you have set up (like your CustomUserDetailsService).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable) // disable CSRF for REST
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // no sessions i.e Token-based authentication (stateless) Every request must carry authentication info (JWT)
            .authorizeHttpRequests(auth -> auth
                    // ✅ Permit all the monitoring and documentation endpoints
                    .requestMatchers(
                            "/actuator/**",
//                            "/actuator/health",
//                            "/actuator/info",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                    ).permitAll()
                    // ✅ Permit POST /login specifically
                    .requestMatchers(HttpMethod.POST, "/login").permitAll()
                    // ✅ Book API rules
                    .requestMatchers(HttpMethod.GET, "/api/v*/books/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v*/customers/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v*/books/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v*/customers/**").hasRole("ADMIN")
                    .requestMatchers("/api/**").hasRole("ADMIN") // 🔒 all other /api/** endpoints require ADMIN
                    // 🔒 everything else requires authentication
                    .anyRequest().authenticated()
            );

        // 🔹 Add JWT filter before Spring Security authentication
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


