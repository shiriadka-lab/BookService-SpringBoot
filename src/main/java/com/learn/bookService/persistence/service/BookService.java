package com.learn.bookService.persistence.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.learn.bookService.Events.BookEventProducer;
import com.learn.bookService.controller.LogController;
import com.learn.bookService.dto.BookDTO;
import com.learn.bookService.dto.BookPatchDTO;
import com.learn.bookService.exception.BookAlreadyExistsException;
import com.learn.bookService.exception.BookNotFoundException;
import com.learn.bookService.metrics.BookMetricsService;
import com.learn.bookService.persistence.model.Book;
import com.learn.bookService.persistence.repo.BookRepository;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;

@Service
public class BookService implements BookOperations {
	
	// creating a logger
    Logger logger
        = LoggerFactory.getLogger(LogController.class);

	    
//    private final Counter bookCreatedCounter;
    

//    public BookService(MeterRegistry meterRegistry, BookRepository bookRepository) {
//        this.bookCreatedCounter = Counter.builder("books.created")
//                .description("Number of books created")
//                .register(meterRegistry);
//        this.bookRepository = bookRepository;
//        
//    }
	  
	  private final BookRepository bookRepository;
	  private final BookEventProducer bookEventProducer;
	  private final BookMetricsService bookMetricsService;
//	  private final Counter bookCreatedCounter;
	  
	  public BookService() {
		  this.bookRepository = null;
		  this.bookEventProducer = null;
		  this.bookMetricsService = null;
	  }

	  public BookService(BookRepository repo,
	                       BookEventProducer eventProducer,
	                       BookMetricsService metrics) {
	      this.bookRepository = repo;
	      this.bookEventProducer = eventProducer;
	      this.bookMetricsService = metrics;
	  }
    
    // TODO : the @Timed metrics do not show up in prometheus.
    // CREATE / UPDATE
    @Timed(value = "books.create.time", description = "Time to create book", extraTags = {"service", "book-service"})
    public BookDTO create(BookDTO bookDto) {
    	Book book = new Book();
    	book.setAuthor(bookDto.getAuthor());
    	book.setPrice(bookDto.getPrice());
    	book.setTitle(bookDto.getTitle());
    	
    	List<Book> existingCustomer = bookRepository.findByTitle(book.getTitle());
        if (existingCustomer.isEmpty()) {
        	bookRepository.save(book);
        } else {
            throw new BookAlreadyExistsException("Book already exists!!");
        }
//        bookCreatedCounter.increment();
        
        bookEventProducer.publishBookCreated(book);
        
        bookMetricsService.recordBookCreated();

        return toResponseDTO(bookRepository.save(book));
    }
    
    @Timed(value = "books.update.time", description = "Time taken to update a book", extraTags = {"service", "book-service"})
    @Override
    public BookDTO update(Long id, BookDTO dto) {

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
    
    @Timed(value = "books.update.time", description = "Time taken to update a book", extraTags = {"service", "book-service"})
    @Override
    public BookDTO updatePatch(Long id, BookPatchDTO dto) {

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

    // READ by ID
    @Timed(value = "books.search.time", description = "Time taken to search a book", extraTags = {"service", "book-service"})
    @Override
    public BookDTO findById(Long id) {	
    	BookDTO book = toResponseDTO(bookRepository
                .findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found")));
        return book;
    }

    // READ all and sort by title
    @Timed(value = "books.search.all.time", description = "Time taken to search all books", extraTags = {"service", "book-service"})
    @Override
    public Iterable<Book> findAll() {
        //return bookRepository.findAll(PageRequest.of(0, 5, Sort.by("title")));
    	return bookRepository.findAll();
    }

    // COUNT
    public long count() {
        return bookRepository.count();
    }

    // DELETE
    @Override
    public void deleteById(Long id) {
    	bookRepository.findById(id)
        .orElseThrow(BookNotFoundException::new);
        bookRepository.deleteById(id);
    }

    @Timed(value = "books.search.time", description = "Time taken to search a book", extraTags = {"service", "book-service"})
    @Override
	public List findByTitle(String bookTitle) {
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

