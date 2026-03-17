package com.learn.bookService.Events;

import java.util.Date;

import org.apache.kafka.common.errors.NetworkException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.learn.bookService.client.PricingClient;
import com.learn.bookService.controller.LogController;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Profile("docker")
@Service
public class BookEventProducer implements BookEventPublisher{
	
	// creating a logger
    Logger log
        = LoggerFactory.getLogger(LogController.class);

	@Autowired
    private final KafkaTemplate<String, BookCreatedEvent> kafkaTemplate;
	
    @Autowired
    private BookMetricsService bookMetricsService;
    
    @Autowired
    private PricingClient pricingClient;  // inject Feign client

    public BookEventProducer(KafkaTemplate<String, BookCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


//    @Retry(name = "pricing-service")
//    @CircuitBreaker(name = "pricing-service", fallbackMethod = "fallbackToKafka")
//    @Override
//    public void publishBookCreated(Book book) {
//        BookCreatedEvent event = new BookCreatedEvent(
//            book.getId(),
//            book.getTitle(),
//            book.getAuthor(),
//            book.getPrice()
//        );
//        
//        try
//        {
//            TestKafkaObject date = new TestKafkaObject();
//            date.setDate(new Date());
//            event.setCreateDate(date);
//
//            kafkaTemplate.send("book-created", event);
//            
//            bookMetricsService.publishedBookCreated();
//        }
//		catch(Exception e)
//		{
//			// Log the exception and continue without throwing it to avoid affecting the main flow
//			// TODO handle specific exceptions (e.g., KafkaException) if needed
//			logger.error("Failed to publish book created event: " + e.getMessage());
//		}
//
//    }
    
    @Retry(name = "pricingService")
    @CircuitBreaker(name = "pricingService", fallbackMethod = "fallbackToKafka")
    @Override
    public void publishBookCreated(Book book) {
        BookCreatedEvent event = new BookCreatedEvent(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice()
        );

        // 1. Try REST first - fast, synchronous
        log.info("Making REST call to pricing service: {}", book.getId());
        pricingClient.notifyBookCreated(event);
        bookMetricsService.publishedBookCreated();
    }
    
    // Fallback - called when REST fails after retries / circuit open
    /**
     *     ## What are your options when the producer genuinely fails?
    This is where it gets interesting. You have three realistic choices:
    **1. Log and accept the loss** — what you're currently doing. Acceptable only if the data is truly throwaway.
    **2. Save to a local database as a fallback-of-fallback** — called the **Outbox Pattern**. 
    *save the failed event to a DB table, and have a scheduled job retry publishing it later. This is the production-grade answer.
    **3. Throw an exception** — bubble it up so the caller knows the book creation truly failed.
    * In your case that might mean the REST response to the client indicates partial failure.

     */

    public void fallbackToKafka(Book book, Throwable throwable) {
        log.warn("REST call to PricingService failed: {}. " +
                 "Falling back to Kafka for book: {}",
                 throwable.getMessage(), book.getId());

        BookCreatedEvent event = new BookCreatedEvent(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice()
        );

        try {
            TestKafkaObject date = new TestKafkaObject();
            date.setDate(new Date());
            event.setCreateDate(date);

            // 2. Fallback to Kafka - guaranteed delivery
            // Send asynchronously and log result in callback to avoid blocking main thread
            // kafka client will handle retries and failures according to its configuration (e.g., retries, acks)
            // if all of that fails,  CompletableFuture completes with exception
            // - we log it but don't throw to avoid crashing book creation
            // send a keyed message to ensure all events for the same book go to the same partition 
            // (important for ordering and idempotency)
            kafkaTemplate.send("book-created", book.getId().toString(), event)
	            .whenComplete((result, ex) -> {
		            if (ex == null) {
		                log.info("Published to Kafka for book: {}", book.getId());
		                return;
		            }
		
		            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
		
		            if (cause instanceof OutOfOrderSequenceException) {
		                // fatal — producer is dead, alert immediately
		                log.error("FATAL: Producer sequence error for book: {}. " +
		                          "All subsequent publishes will fail. " +
		                          "App restart required.", book.getId());
		                // save to outbox DB
		              //  saveToOutbox(book);
		                // alert ops team — PagerDuty, Slack alert, metric increment
		               // alertOpsTeam("Kafka producer fatal error — restart required");
		
		            } else if (cause instanceof TimeoutException
		                    || cause instanceof NetworkException) {
		                // transient — already retried by client, save to outbox
		                log.warn("Transient Kafka failure for book: {}. " +
		                         "Saving to outbox.", book.getId());
		               // saveToOutbox(book);
		
		            } else {
		                // unknown — log and save to outbox
		                log.error("Unexpected Kafka failure for book: {}. Cause: {}",
		                          book.getId(), cause.getMessage());
		               //saveToOutbox(book);
		            }
	            });
            bookMetricsService.publishedBookCreated();
        } catch (Exception e) {
            // Both REST and Kafka failed - log but don't crash book creation
            log.error("Both REST and Kafka failed for book: {}. Cause: {}",
                      book.getId(), e.getMessage());
        }
    }
}