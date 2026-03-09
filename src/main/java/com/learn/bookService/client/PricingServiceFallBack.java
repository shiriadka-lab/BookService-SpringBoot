package com.learn.bookService.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.learn.bookService.Events.BookCreatedEvent;

@Component
public class PricingServiceFallBack implements PricingClient {

	private static final Logger log = LoggerFactory.getLogger(PricingServiceFallBack.class);
	@Override
	public List<BookPriceResponse> getAll() {
		// TODO Auto-generated method stub
		log.error("pricing-service is down, could not notify for book: {}");
		return null;
	}

	@Override
	public ResponseEntity<String> notifyBookCreated(BookCreatedEvent event) {
		// TODO Auto-generated method stub
		log.error("pricing-service is down, could not notify for book: {}", event.getAuthor());
		return null;
	}
}