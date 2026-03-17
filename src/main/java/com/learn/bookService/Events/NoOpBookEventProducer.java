package com.learn.bookService.Events;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.learn.bookService.client.PricingClient;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Profile("!docker")
@Service
public class NoOpBookEventProducer implements BookEventPublisher {

	  private final BookMetricsService bookMetricsService;
	  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NoOpBookEventProducer.class);

	    @Autowired
	    public NoOpBookEventProducer(BookMetricsService bookMetricsService) {
	        this.bookMetricsService = bookMetricsService;
	    }
	    
	    @Autowired
	    private PricingClient pricingClient;  // Spring injects the generated impl


	    @Override
	    public void publishBookCreated(Book book) {
	        // Do nothing, or just log
	        System.out.println("Skipping Kafka publish for book " + book.getId());
	        BookCreatedEvent event = new BookCreatedEvent(
		            book.getId(),
		            book.getTitle(),
		            book.getAuthor(),
		            book.getPrice()
		        );
	     // Just call it like any normal method!
	        try {
	          //  pricingClient.notifyBookCreated(event);
	        	// 2. Notify pricing service - protected by circuit breaker + retry
	            notifyPricingService(book);
	            System.out.println("Notified pricing service for book " + book.getId());
	        } catch (Exception e) {
	            // Book is already saved — don't fail because pricing service is down
	            System.out.println("Failed to notify pricing service: " + e.getMessage());
	        }

	        bookMetricsService.publishedBookCreated();
	    }
	    
	 // ── Extract the pricing notification into its own method ─────────────────
	    // @Retry fires FIRST  — tries up to 3 times with 2s delay between attempts
	    // @CircuitBreaker fires AFTER retries — opens circuit if 50%+ calls fail
	    //
	    // The fallbackMethod is called when:
	    //   - All 3 retries are exhausted
	    //   - OR the circuit is already OPEN (fails fast, skips retries)

	    @Retry(name = "pricingService")
	    @CircuitBreaker(name = "pricingService", fallbackMethod = "notifyPricingServiceFallback")
	    public void notifyPricingService(Book book) {
	        log.info("Notifying pricing service for book: {}", book.getId());
	        BookCreatedEvent event = new BookCreatedEvent(
		            book.getId(),
		            book.getTitle(),
		            book.getAuthor(),
		            book.getPrice()
		        );
	        pricingClient.notifyBookCreated(event);
	        log.info("Pricing service notified successfully for book: {}", book.getId());
	    }

	    // ── Fallback method ───────────────────────────────────────────────────────
	    // IMPORTANT RULES for fallback methods:
	    //   1. Must be in the SAME class as the @CircuitBreaker method
	    //   2. Must have the SAME parameters PLUS a Throwable as the last parameter
	    //   3. Must have the SAME return type (void here)
	    //   4. Name must match fallbackMethod = "..." in the annotation

	    public void notifyPricingServiceFallback(Book book, Throwable throwable) {
	        log.warn("Circuit breaker triggered for book: {}. Cause: {}. " +
	                 "Book was created successfully. Pricing notification skipped.",
	                 book.getId(), throwable.getMessage());
	        // Book creation still succeeds - we just skip the pricing notification
	    }

}