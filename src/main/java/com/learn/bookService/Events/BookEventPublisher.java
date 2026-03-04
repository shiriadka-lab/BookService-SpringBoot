package com.learn.bookService.Events;

import com.learn.bookService.persistence.model.Book;

public interface BookEventPublisher {
    void publishBookCreated(Book book);
}