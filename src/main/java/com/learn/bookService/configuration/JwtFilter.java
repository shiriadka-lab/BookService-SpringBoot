package com.learn.bookService.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnBean(JwtUtil.class)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

    	// Extract the Authorization header from the incoming request
        String authHeader = request.getHeader("Authorization");

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

        	// Extract the JWT token from the Authorization header by removing the "Bearer " prefix
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);

                // If a username was successfully extracted and there is no existing authentication in the security context
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Validate the token using the JwtUtil. This checks if the token is valid and not expired.
                    if (jwtUtil.validateToken(token, userDetails)) {

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Set the authentication in the security context, which allows the user to access protected resources
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        // token invalid according to JwtUtil
                        logger.warn("JWT token validation failed for user {}", username);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                        return;
                    }
                }

            } catch (JwtException | IllegalArgumentException e) {
                // JWT parsing/validation errors
                logger.warn("Failed to parse/validate JWT token: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                return;
            } catch (Exception e) {
                // any other unexpected error - don't leak details
                logger.error("Unexpected error while processing JWT token", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error processing authentication");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}