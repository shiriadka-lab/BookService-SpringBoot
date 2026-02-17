package com.learn.bookService.exception;

public class BookIdMismatchException extends RuntimeException {

	public BookIdMismatchException() {
        super("Book ID Mismatch");
    }
    public BookIdMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
    // ...
}
