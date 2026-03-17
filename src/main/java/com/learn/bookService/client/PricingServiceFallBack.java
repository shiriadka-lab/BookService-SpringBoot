package com.learn.bookService.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.learn.bookService.Events.BookCreatedEvent;

//This class is called automatically by Resilience4j when:
//- PricingService is unreachable (circuit OPEN)
//- All 3 retry attempts have failed
//- A FeignException or ConnectException is thrown
//
//Book creation will still SUCCEED - this just logs the failure gracefully.
@Component
public class PricingServiceFallBack implements PricingClient {

	private static final Logger log = LoggerFactory.getLogger(PricingServiceFallBack.class);
	@Override
	public List<BookPriceResponse> getAll() {
		// TODO Auto-generated method stub
		log.error("pricing-service is down, could not notify for book: {}");
		return null;
	}

//	@Override
//	public ResponseEntity<String> notifyBookCreated(BookCreatedEvent event) {
//		// TODO Auto-generated method stub
//		log.error("pricing-service is down, could not notify for book: {}", event.getAuthor());
//		return null;
//	}
	
	@Override
    public void notifyBookCreated(BookCreatedEvent event) {
        // Log the failure - book creation continues normally
        log.warn("PricingService is unavailable. Could not notify for book: {}. " +
                 "Circuit breaker fallback triggered. " +
                 "Pricing will be updated when service recovers.", event.getTitle());

        // ── Do NOT throw an exception here ──
        // Returning void means BookService continues normally
        // The book gets created, pricing notification is just skipped
    }
}