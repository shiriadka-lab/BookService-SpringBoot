package com.learn.bookService.Events;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;

@Service
public class BookEventProducer {

	@Autowired
    private final KafkaTemplate<String, BookCreatedEvent> kafkaTemplate;
	
    @Autowired
    private BookMetricsService bookMetricsService;

    public BookEventProducer(KafkaTemplate<String, BookCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookCreated(Book book) {
        BookCreatedEvent event = new BookCreatedEvent(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice()
        );
        
        TestKafkaObject date = new TestKafkaObject();
        date.setDate(new Date());
        event.setCreateDate(date);

        kafkaTemplate.send("book-created", event);
        
        bookMetricsService.publishedBookCreated();
    }
}



