package com.learn.bookService.Events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.learn.bookService.controller.LogController;

import jakarta.annotation.PostConstruct;

@Service
@Profile("docker")
public class BookEventConsumer {

    Logger logger
       = LoggerFactory.getLogger(LogController.class);
    
    @PostConstruct
    public void init() {
        logger.info("BookEventConsumer bean created");
    }
    
    @KafkaListener(topics = "book-created", groupId = "book-service-group" )
    public void consume(BookCreatedEvent event) {
       logger.info("Received BookCreatedEvent: {}", event.toString());
    }
}
