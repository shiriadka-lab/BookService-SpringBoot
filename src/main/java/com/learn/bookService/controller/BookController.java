package com.learn.bookService.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.learn.bookService.dto.BookDTO;
import com.learn.bookService.dto.BookPatchDTO;
import com.learn.bookService.persistence.service.BookOperations;

import jakarta.validation.Valid;

//-> specialized version of @Controller that automatically serializes return objects into JSON/XML responses. 
// It is equivalent to @Controller + @ResponseBody.
@RestController     
@RequestMapping("/api/v1/books")
public class BookController {
	
	// creating a logger
    Logger logger
        = LoggerFactory.getLogger(LogController.class);
    
    private final BookOperations bookOps;

    public BookController(BookOperations bookOps) {
        this.bookOps = bookOps;
    }

    

    @GetMapping
    public Iterable findAll() {
        return bookOps.findAll();
    }

//    @GetMapping("/title/{bookTitle}")
//    public List findByTitle(@PathVariable String bookTitle) {
//        return bookRepository.findByTitle(bookTitle);
//    }
    
    @GetMapping("/search")
    public List findByTitle( @RequestParam("title") String bookTitle) {
    	return bookOps.findByTitle(bookTitle);
    	

    }
   

    @GetMapping("/{id}")
    public BookDTO findById(@PathVariable Long id) {
        BookDTO bookDTO = bookOps.findById(id);
        return bookDTO;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // @Valid: This annotation triggers validation on the Book object.
    public BookDTO create(@Valid @RequestBody BookDTO bookDto) {
    	
    	BookDTO newBook = bookOps.create(bookDto);
        return newBook;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
    	bookOps.deleteById(id);
    }

    /*
     * PUT -> Replaces the entire resource
		Client must send all fields
		Missing fields are treated as null / default
     */
    @PutMapping("/{id}")
    public BookDTO updateBook(@Valid @RequestBody BookDTO bookDto, @PathVariable Long id) {
    	logger.info("update Book with ID " + id);

        BookDTO updated = bookOps.update(id, bookDto);
        // Save
        return updated;
    }
    
    /* PATCH -> 
     * Updates only the provided fields
		Missing fields are ignored
		No data loss
		Have a separate DTO for patch functions
     */
    @PatchMapping("/{id}")
    public BookDTO updatePartialBook( @RequestBody BookPatchDTO bookDto, @PathVariable Long id) {
    	logger.info("update Book with ID " + id);

        BookDTO updated = bookOps.updatePatch(id, bookDto);
        // Save
        return updated;
    }
    
//    // Adding exception handlers for BookNotFoundException 
//    @ExceptionHandler(value = BookNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ErrorResponse handleBookNotFoundException(BookNotFoundException ex) {
//        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
//    }
    
//    @ExceptionHandler(value = BookAlreadyExistsException.class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ErrorResponse handleBookAlreadyExistsException(BookAlreadyExistsException ex) {
//        return new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
//    }

}

