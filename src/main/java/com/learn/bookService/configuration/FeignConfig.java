package com.learn.bookService.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.auth.BasicAuthRequestInterceptor;

// This configuration class sets up Feign to use basic authentication when communicating with the Pricing Service.
// By defining a BasicAuthRequestInterceptor bean, we ensure that every request made by Feign to the Pricing Service will include the necessary authentication headers.
// Note: In a production application, you should not hardcode credentials in your code. Instead, consider using environment variables or a secure vault to manage sensitive information.
@Configuration
public class FeignConfig {

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("admin", "secret123");
    }
}
