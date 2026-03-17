package com.learn.bookService.persistence.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.learn.bookService.persistence.model.Customer;

// This class provides static methods to create Specifications for filtering Customer entities based on various criteria
// Each method returns a Specification that can be used in the CustomerRepository to query the database with dynamic filters
// For example, you can use these specifications to find customers by country, active status, or minimum age
public class CustomerSpecs {

	// This method returns a Specification to filter customers by country
	// If the country parameter is null, it returns null, which means no filtering will be applied for the country
    public static Specification<Customer> hasCountry(String country) {
        return (root, query, cb) ->
            country == null ? null : cb.equal(root.get("country"), country);
    }
    
    public static Specification<Customer> hasCity(String city) {
        return (root, query, cb) ->
        	city == null ? null : cb.equal(root.get("city"), city);
    }

    public static Specification<Customer> isActive(Boolean active) {
        return (root, query, cb) ->
            active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Customer> minAge(Integer age) {
        return (root, query, cb) ->
            age == null ? null : cb.greaterThanOrEqualTo(root.get("age"), age);
    }
    
    public static Specification<Customer> createdBefore(LocalDateTime createdAt ) {
        return (root, query, cb) ->
        createdAt == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), createdAt);
    }
    
    public static Specification<Customer> createdAfter(LocalDateTime createdAt ) {
        return (root, query, cb) ->
        createdAt == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), createdAt);
    }
    
    public static Specification<Customer> maxAge(Integer age) {
        return (root, query, cb) ->
            age == null ? null : cb.greaterThanOrEqualTo(root.get("age"), age);
    }
    
    public static Specification<Customer> ageRange(Integer minAge, Integer maxAge) {
    	return (root, query, cb) -> {
            if (minAge == null && maxAge == null) {
                return null; // no filtering
            }
            if (minAge != null && maxAge != null) {
                return cb.between(root.get("age"), minAge, maxAge);
            }
            if (minAge != null) {
                return cb.greaterThanOrEqualTo(root.get("age"), minAge);
            }
            return cb.lessThanOrEqualTo(root.get("age"), maxAge);
        };

    }
    
    // This method returns a Specification to filter customers based on a keyword that can match multiple fields
    // (firstName, lastName, email, city)
    public static Specification<Customer> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            // handle case-insensitive search by converting both the field and the keyword to lower case
            // The pattern is constructed to allow for partial matches (e.g., searching for "john" would match "John", "Johnson", etc.)
            // The '%' characters are wildcards in SQL that match any sequence of characters, allowing for flexible searching
            // For example, if the keyword is "john", the pattern will be "%john%",\
            // which will match any string that contains "john" regardless of its position in the field. startwith "john" or ends with "john" or has "john" in the middle.
            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("city")), pattern)
            );
        };
    }
    
    public static LocalDateTime parseDateOrDateTime(String value, boolean endOfDay) {
        if (value == null) return null;

        // If the user provided a full timestamp
        if (value.contains("T")) {
            return LocalDateTime.parse(value);
        }

        // If only a date was provided
        LocalDate date = LocalDate.parse(value);
        return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
    }
    
    public static Specification<Customer> afterId(Long after) {
        return (root, query, cb) ->
            after == null ? null : cb.greaterThan(root.get("id"), after);
    }
    
    public static Specification<Customer> beforeId(Long before) {
        return (root, query, cb) ->
            before == null ? null : cb.lessThan(root.get("id"), before);
    }



}