package com.learn.bookService.persistence.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.learn.bookService.persistence.model.Customer;

// JpaSpecificationExecutor is used to support dynamic queries based on specifications, 
// allowing for more flexible and complex querying capabilities
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
	// user Optional to handle the case where a customer with the given email may not exist
	// This method will return an Optional containing the Customer if found, or an empty Optional if not found
    Optional<Customer> findByEmail(String email);
    
    // Use list to return multiple customers that match the criteria, 
    // as there may be multiple customers from the same country, with the same active status, or above a certain age
    List<Customer> findByCountry(String country);
    Page<Customer> findAll(Pageable pageable);
    List<Customer> findByIsActive(boolean isActive);
    List<Customer> findByAgeGreaterThanEqual(int age);
    List<Customer> findByCountryAndAge(String country,int age);
    
    @Query("""
        SELECT c FROM Customer c
        WHERE (:country IS NULL OR c.country = :country)
          AND (:active IS NULL OR c.isActive = :active)
          AND (:age IS NULL OR c.age >= :age)
    """)
    Page<Customer> search(
            @Param("country") String country,
            @Param("active") Boolean active,   // Use Boolean to allow null values for optional filtering
            @Param("age") Integer age,         // Use Integer to allow null values for optional filtering
            Pageable pageable
    );
    
    // This method uses PostgreSQL's full-text search capabilities to search across multiple fields
    // (first_name, last_name, email) for the given keyword.
    // The to_tsvector function converts the concatenated fields into a tsvector, 
    // which is a data type optimized for text search.
    // The plainto_tsquery function converts the input keyword into a tsquery, which is used to match against the tsvector.
    // The @@ operator checks if the tsvector matches the tsquery.
    // The countQuery is used to get the total number of results for pagination purposes.
    // Note: This query is specific to PostgreSQL and may not work with other databases without modification.
    // The nativeQuery = true attribute indicates that this is a native SQL query, not a JPQL query.
    @Query(value = """
        SELECT *
        FROM customer
        WHERE to_tsvector('english', first_name || ' ' || last_name || ' ' || email)
              @@ plainto_tsquery('english', :keyword)
        """,
        countQuery = """
        SELECT count(*)
        FROM customer
        WHERE to_tsvector('english', first_name || ' ' || last_name || ' ' || email)
              @@ plainto_tsquery('english', :keyword)
        """,
        nativeQuery = true)
    Page<Customer> searchFullText(@Param("keyword") String keyword, Pageable pageable);
    
    // This method implements keyset pagination by using the customer's ID as a cursor.
    // The query retrieves customers with an ID greater than the specified "after" parameter,
    // allowing for efficient pagination without the performance issues associated with offset-based pagination.
    // The results are ordered by ID in ascending order to ensure consistent pagination.
    @Query("""
    	    SELECT c FROM Customer c
    	    WHERE (:after IS NULL OR c.id > :after)
    	    ORDER BY c.id ASC
    	    """)
   	List<Customer> findNextPage(Long after, Pageable pageable);

    
}
