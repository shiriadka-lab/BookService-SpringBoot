package com.learn.bookService.Events;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.learn.bookService.client.PricingClient;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;

//@Profile("!docker")
@Service
public class NoOpBookEventProducer implements BookEventPublisher {

	  private final BookMetricsService bookMetricsService;

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
	            pricingClient.notifyBookCreated(event);
	            System.out.println("Notified pricing service for book " + book.getId());
	        } catch (Exception e) {
	            // Book is already saved — don't fail because pricing service is down
	            System.out.println("Failed to notify pricing service: " + e.getMessage());
	        }

	        bookMetricsService.publishedBookCreated();
	    }

}