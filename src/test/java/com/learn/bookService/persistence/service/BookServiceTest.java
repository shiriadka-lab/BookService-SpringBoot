package com.learn.bookService.persistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.learn.bookService.Events.BookEventPublisher;
import com.learn.bookService.client.PricingClient;
import com.learn.bookService.dto.BookDTO;
import com.learn.bookService.exception.BookAlreadyExistsException;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;
import com.learn.bookService.persistence.repo.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repo;

    @Mock
    private BookEventPublisher publisher;

    @Mock
    private BookMetricsService metrics;

    private BookService service;
    
    @MockBean
    private PricingClient pricingClient;  // Mock it so URL is never needed

    @BeforeEach
    void setUp() {
        service = new BookService(repo, publisher, metrics);
    }

    @Test
    void create_saves_and_publishes() {
        BookDTO dto = new BookDTO();
        dto.setTitle("T");
        dto.setAuthor("A");
        dto.setPrice(BigDecimal.TEN);

        when(repo.findByTitle("T")).thenReturn(Collections.emptyList());

        Book saved = new Book();
        // set id via reflection helper (no public setter required)
        setId(saved, 1L);
        saved.setTitle("T");
        saved.setAuthor("A");
        saved.setPrice(BigDecimal.TEN);

        when(repo.save(any(Book.class))).thenReturn(saved);

        BookDTO result = service.create(dto);

        assertEquals(1L, result.getId());
        assertEquals("T", result.getTitle());

        verify(repo).save(any(Book.class));
        verify(publisher).publishBookCreated(saved);
        verify(metrics).recordBookCreated();
    }

    @Test
    void create_throws_if_exists() {
        BookDTO dto = new BookDTO();
        dto.setTitle("T");

        Book existing = new Book();
        // set id via reflection helper (no public setter required)
        setId(existing, 2L);
        existing.setTitle("T");

        when(repo.findByTitle("T")).thenReturn(List.of(existing));

        assertThrows(BookAlreadyExistsException.class, () -> service.create(dto));

        verify(repo, never()).save(any());
        verify(publisher, never()).publishBookCreated(any());
    }

    // Reflection helper to set private id field on entity for tests
    private static void setId(Book book, Long id) {
        try {
            Field f = Book.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(book, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id on Book via reflection", e);
        }
    }
}