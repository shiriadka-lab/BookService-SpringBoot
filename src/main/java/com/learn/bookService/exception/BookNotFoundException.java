package com.learn.bookService.exception;

public class BookNotFoundException extends RuntimeException {

	public BookNotFoundException() {
        super("Book not found"); // optional default message
    }
	
	public BookNotFoundException(String message) {
        super(message, null); // optional default message
    }
	
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    // ...
}
