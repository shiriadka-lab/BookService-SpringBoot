package com.learn.bookService.exception;

public class BookAlreadyExistsException extends RuntimeException {

	public BookAlreadyExistsException() {
        super("Book ALREADY EXISTS");
    }
    public BookAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    // ...
	public BookAlreadyExistsException(String message) {
		super(message, null);
	}
}
