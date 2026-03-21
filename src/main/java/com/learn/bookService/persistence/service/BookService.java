package com.learn.bookService.persistence.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.learn.bookService.Events.BookEventPublisher;
import com.learn.bookService.configuration.CacheConstants;
import com.learn.bookService.dto.BookDTO;
import com.learn.bookService.dto.BookPatchDTO;
import com.learn.bookService.exception.BookAlreadyExistsException;
import com.learn.bookService.exception.BookNotFoundException;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;
import com.learn.bookService.persistence.repo.BookRepository;

import io.micrometer.core.annotation.Timed;

@Service
public class BookService implements BookOperations {
    
    // creating a logger - use this class for the logger name
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final BookEventPublisher bookEventPublisher;
    private final BookMetricsService bookMetricsService;

    // single-constructor injection (no @Autowired needed)
    public BookService(BookRepository repo,
                       BookEventPublisher eventPublisher,
                       BookMetricsService metrics) {
        this.bookRepository = repo;
        this.bookEventPublisher = eventPublisher;
        this.bookMetricsService = metrics;
    }
    
    // CREATE / UPDATE
    //  Evict cache on create
    @CacheEvict(value = CacheConstants.BOOKS_CACHE, allEntries = true)
    @Timed(value = "books.create.time", description = "Time to create book", extraTags = {"service", "book-service"})
    public BookDTO create(BookDTO bookDto) {
    	logger.info("Cache EVICT — create book {}", bookDto);
        Book book = new Book();
        book.setAuthor(bookDto.getAuthor());
        book.setPrice(bookDto.getPrice());
        book.setTitle(bookDto.getTitle());
        
        List<Book> existingCustomer = bookRepository.findByTitle(book.getTitle());
        if (!existingCustomer.isEmpty()) {
            throw new BookAlreadyExistsException("Book already exists!!");
        }
        
        // persist once
        Book saved = bookRepository.save(book);

        // publish and metric after successful save
        bookEventPublisher.publishBookCreated(saved);
        bookMetricsService.recordBookCreated();

        return toResponseDTO(saved);
    }
    
    
    // Evict cache on update
    @CacheEvict(value = CacheConstants.BOOKS_CACHE, allEntries = true)
    @Timed(value = "books.update.time", description = "Time taken to update a book", extraTags = {"service", "book-service"})
    @Override
    public BookDTO update(Long id, BookDTO dto) {

    	logger.info("Cache EVICT — Update book {}", id);
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() ->
                new BookNotFoundException("NO BOOK FOUND WITH ID = " + id));
        
        bookRepository.findByTitle(
                dto.getTitle()).
                    stream().
                    filter(b -> !b.getId().equals(id)).
                    findAny().
                    ifPresent(b -> {
                        throw new BookAlreadyExistsException("Book with title '" + dto.getTitle() + "' already exists");
        });

        logger.info("Book with Id {} found ", (id));
        existingBook.setTitle(dto.getTitle());
        existingBook.setAuthor(dto.getAuthor());
        existingBook.setPrice(dto.getPrice());

        logger.info("Updating book " + existingBook.toString());
        bookMetricsService.recordBookUpdated();
        return  toResponseDTO(bookRepository.save(existingBook));
    }
    
    
    //  Evict cache on patch
    @CacheEvict(value = CacheConstants.BOOKS_CACHE, allEntries = true)
    @Timed(value = "books.update.time", description = "Time taken to update a book", extraTags = {"service", "book-service"})
    @Override
    public BookDTO updatePatch(Long id, BookPatchDTO dto) {

    	logger.info("Cache EVICT — Update book {}", id);
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() ->
                new BookNotFoundException("NO BOOK FOUND WITH ID = " + id));

        if(null != dto.getTitle()) {
            existingBook.setTitle(dto.getTitle());
        }
        if(null != dto.getAuthor()) {
            existingBook.setAuthor(dto.getAuthor());
        }
        if(null != dto.getPrice()) {
            existingBook.setPrice(dto.getPrice());
        }
        bookMetricsService.recordBookUpdated();
        return toResponseDTO(bookRepository.save(existingBook));
    }
    
    // Cache individual book by ID
    @Cacheable(value = CacheConstants.BOOKS_CACHE, key = "#id")
    // READ by ID
    @Timed(value = "books.search.time", description = "Time taken to search a book", extraTags = {"service", "book-service"})
    @Override
    public BookDTO findById(Long id) {
        logger.info("Cache MISS — fetching book {} from DB", id);
        BookDTO book = toResponseDTO(bookRepository
                .findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found")));
        return book;
    }

 // Cache all books
    @Cacheable(value = CacheConstants.BOOKS_CACHE, key = "'all'")
    // READ all and sort by title
    @Timed(value = "books.search.all.time", description = "Time taken to search all books", extraTags = {"service", "book-service"})
    @Override
    public List<BookDTO> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // COUNT
    public long count() {
        return bookRepository.count();
    }

    // DELETE
    // Evict cache on delete
    @CacheEvict(value = CacheConstants.BOOKS_CACHE, allEntries = true)
    @Override
    public void deleteById(Long id) {
    	logger.info("Cache EVICT — Delete book {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(BookNotFoundException::new);

        bookRepository.delete(book);
    }

    @Timed(value = "books.search.time", description = "Time taken to search a book", extraTags = {"service", "book-service"})
    @Override
    public List<BookDTO> findByTitle(String bookTitle) {
        // TODO Auto-generated method stub
        return bookRepository.findByTitle(bookTitle)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
    
    private BookDTO toResponseDTO(Book book) {
        BookDTO bookDtoResp = new BookDTO();
        bookDtoResp.setAuthor(book.getAuthor());
        bookDtoResp.setTitle(book.getTitle());
        bookDtoResp.setPrice(book.getPrice());
        bookDtoResp.setId(book.getId());

        return bookDtoResp;
    }


}