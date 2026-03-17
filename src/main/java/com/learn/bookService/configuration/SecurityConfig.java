package com.learn.bookService.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

// ── CHANGES FROM ORIGINAL ─────────────────────────────────────────────────────
// 1. Removed JwtFilter and JwtUtil - replaced by Spring OAuth2 Resource Server
// 2. Added jwkSetUri pointing to Auth Server JWKS endpoint
// 3. Added jwtAuthenticationConverter to read "roles" claim (your existing claim name)
// 4. All @PreAuthorize rules and requestMatchers UNCHANGED
// ─────────────────────────────────────────────────────────────────────────────

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // keeps @PreAuthorize working
public class SecurityConfig {

    // Auth Server JWKS endpoint - BookService fetches public key from here
    // to verify tokens signed by Auth Server's private key
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── These are UNCHANGED from your original config ──
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // ── Removed /login - it's now in Auth Server ──
                .requestMatchers(HttpMethod.GET, "/api/v*/books/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v*/customers/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v*/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v*/customers/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // ── Replace JwtFilter with OAuth2 Resource Server ──
            // This validates JWT using public key fetched from Auth Server JWKS endpoint
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        // ── JwtFilter is REMOVED - OAuth2 Resource Server replaces it ──
        // http.addFilterBefore(jwtFilter, ...) ← DELETE this line

        return http.build();
    }

    // ── Fetches Auth Server public key to verify incoming user tokens ─────────
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    // ── Reads "roles" claim from JWT and converts to Spring Security authorities
    // IMPORTANT: your JwtUtil used "roles" as the claim name - this matches it
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");  // matches your existing "roles" claim
        authoritiesConverter.setAuthorityPrefix("");             // roles already have ROLE_ prefix

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


