package com.learn.bookService.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import com.learn.bookService.persistence.model.Book;

// Book is the Entity type 
// Long is Type of the primary key (@Id)
// By extending CrudRepository, you automatically get methods like (No implementation needed):
	//
	//save(Book book)
	//findById(Long id)
	//findAll()
	//deleteById(Long id)
	//count()
	//existsById(Long id)  
//public interface BookRepository extends CrudRepository<Book, Long> {
/**
 * CrudRepository = basic CRUD only.
	JpaRepository = CrudRepository + sorting + pagination + batch operations + JPA-specific features.
	Always prefer JpaRepository unless you really want a minimal interface.
 */

public interface BookRepository extends JpaRepository<Book, Long> {
	
	//This is Spring Data Query Method Derivation.
	// Spring reads the method name and generates a query automatically.
	// SELECT * FROM book WHERE title = ?
    List<Book> findByTitle(String title);
    
    Optional<Book> findByAuthor(String author);
    
    
}