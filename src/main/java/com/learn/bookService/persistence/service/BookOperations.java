package com.learn.bookService.persistence.service;

import java.util.List;

import com.learn.bookService.dto.BookDTO;
import com.learn.bookService.dto.BookPatchDTO;
import com.learn.bookService.persistence.model.Book;

public interface BookOperations {
    BookDTO findById(Long id);
    List<BookDTO> findByTitle(String title);
    BookDTO create(BookDTO dto);
    BookDTO update(Long id, BookDTO dto);
    BookDTO updatePatch(Long id, BookPatchDTO dto);
    void deleteById(Long id);
    Iterable<Book> findAll();
    
}
