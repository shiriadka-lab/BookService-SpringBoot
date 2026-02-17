package com.learn.bookService.metrics;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class BookMetricsService {

    private final MeterRegistry meterRegistry;

    public BookMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordBookCreated() {
        meterRegistry.counter("books.created").increment();
    }
    
    public void recordBookUpdated() {
        meterRegistry.counter("books.updated").increment();
    }
    
    public void publishedBookCreated() {
        meterRegistry.counter("kafka.publish.books.created").increment();
    }

}