package com.learn.bookService.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.learn.bookService.Events.BookCreatedEvent;
import com.learn.bookService.configuration.FeignConfig;

// This interface defines the contract for communication with the Pricing Service using Feign.
//@FeignClient(name = "pricing-service", url = "${pricing.service.url}", configuration = FeignConfig.class)
@FeignClient(name = "pricing-service", configuration = FeignConfig.class, fallback = PricingServiceFallBack.class)
public interface PricingClient {

    @GetMapping("/api/v1/pricing")
    List<BookPriceResponse> getAll();

    // New endpoint
    @PostMapping("/api/v1/pricing/book-created")
    ResponseEntity<String> notifyBookCreated(
                            @RequestBody BookCreatedEvent event);
}
