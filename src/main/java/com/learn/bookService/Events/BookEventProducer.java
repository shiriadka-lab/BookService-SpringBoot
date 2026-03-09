package com.learn.bookService.Events;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.learn.bookService.controller.LogController;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;

//@Profile("docker")
//@Service
//public class BookEventProducer implements BookEventPublisher{
//	
//	// creating a logger
//    Logger logger
//        = LoggerFactory.getLogger(LogController.class);
//
//	@Autowired
//    private final KafkaTemplate<String, BookCreatedEvent> kafkaTemplate;
//	
//    @Autowired
//    private BookMetricsService bookMetricsService;
//
//    public BookEventProducer(KafkaTemplate<String, BookCreatedEvent> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
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
//}