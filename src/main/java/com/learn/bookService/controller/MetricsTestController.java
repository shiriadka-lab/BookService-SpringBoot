package com.learn.bookService.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.bookService.metrics.BookMetricsService;

@RestController
@RequestMapping("/test")
public class MetricsTestController {
	
	 private final BookMetricsService bookMetricsService;

	    public MetricsTestController(BookMetricsService bookMetricsService) {
	        this.bookMetricsService = bookMetricsService;
	    }

	    @GetMapping("/increment")
	    public String increment() {
	        bookMetricsService.recordBookCreated();
	        return "Incremented!";
	    }

}
