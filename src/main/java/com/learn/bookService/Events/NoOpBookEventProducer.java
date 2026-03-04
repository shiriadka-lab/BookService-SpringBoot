package com.learn.bookService.Events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;

@Profile("!docker")
@Service
public class NoOpBookEventProducer implements BookEventPublisher {

	  private final BookMetricsService bookMetricsService;

	    @Autowired
	    public NoOpBookEventProducer(BookMetricsService bookMetricsService) {
	        this.bookMetricsService = bookMetricsService;
	    }

	    @Override
	    public void publishBookCreated(Book book) {
	        // Do nothing, or just log
	        System.out.println("Skipping Kafka publish for book " + book.getId());
	        bookMetricsService.publishedBookCreated();
	    }

}