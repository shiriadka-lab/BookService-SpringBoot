package com.learn.bookService.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learn.bookService.dto.CustomerDTO;
import com.learn.bookService.dto.CustomerMapper;
import com.learn.bookService.dto.PageResponse;
import com.learn.bookService.persistence.model.Customer;
import com.learn.bookService.persistence.repo.CursorPage;
import com.learn.bookService.persistence.repo.CustomerRepository;
import com.learn.bookService.persistence.repo.CustomerSpecs;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository repo;

//    @GetMapping
//    public List<CustomerDTO> list() {
//        return repo.findAll().stream().map(CustomerMapper::toDto).collect(Collectors.toList());
//    }

    /**
     * Example: /api/v1/customers?page=0&size=10&sort=firstName,asc
     * - page: zero-based page index (0..N)
     * - size: the size of the page to be returned
     * - sort: sorting criteria in the format: property,(asc|desc). Default sort
     * @param page
     * @param size
     * @param sort
     * @return
     */
    @GetMapping
    public PageResponse<CustomerDTO> list(
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
    	
//    	  sort[0] = field, sort[1] = direction
      String sortField = sort[0];
      Sort.Direction direction =
              sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                      ? Sort.Direction.DESC
                      : Sort.Direction.ASC;

      // Create a Pageable object with the specified page, size, and sorting
      // Pageable is an interface that provides pagination information to the repository method
      // PageRequest is an implementation of Pageable that allows us to specify the page number, page size, and sorting criteria.
      Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
      // Call the repository method to retrieve a page of customers based on the pageable object
      // Repository method findAll(pageable) triggers a paginated SQL query
      // Spring Data JPA sees that you passed a Pageable, so it generates two SQL queries:
      // Query 1 — fetch the page content: SELECT * FROM customers ORDER BY first_name ASC LIMIT 10 OFFSET 0
      // Query 2 — count total records: SELECT COUNT(*) FROM customers
      //  This is how Spring knows that 1. total # of records, 2. total pages 3. whether next/previous pages exist.
      // The result is a Page object that contains the content of the page (list of customers) 
      // and pagination metadata (total elements, total pages, etc.)
      //RESULT : Spring Data wraps the results in a Page object
//      Page<Customer> contains:
//    	  - getContent() → the list of customers
//    	  - getTotalElements() → total rows in DB
//    	  - getTotalPages() → total pages
//    	  - getNumber() → current page index
//    	  - getSize() → page size
//    	  - hasNext(), hasPrevious(), etc.
      // Map the Page<Customer> to Page<CustomerDTO> using the CustomerMapper

        Page<CustomerDTO> pageResult = repo.findAll(pageable).map(CustomerMapper::toDto);

        return new PageResponse<>(
            pageResult.getContent(),
            pageResult.getNumber(),
            pageResult.getSize(),
            pageResult.getTotalElements(),
            pageResult.getTotalPages()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(CustomerMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody CustomerDTO dto) {
        if (dto.getEmail() != null && repo.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        Customer saved = repo.save(CustomerMapper.fromDto(dto));
        return ResponseEntity.ok(CustomerMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        return repo.findById(id).map(existing -> {
            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setPhone(dto.getPhone());
            existing.setAge(dto.getAge());
            existing.setCity(dto.getCity());
            existing.setCountry(dto.getCountry());
            existing.setActive(dto.isActive());
            existing.setStatus(dto.getStatus());
            Customer saved = repo.save(existing);
            return ResponseEntity.ok(CustomerMapper.toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repo.findById(id).map(c -> {
            repo.delete(c);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
    
    // /api/v1/customers/search?country=Canada&active=true&minAge=25
    @GetMapping("/customerByFilter")
    public PageResponse<CustomerDTO> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
    	
    	// ** can be implemented in multiple ways: 
//    	@GetMapping("/customerByFilter")
//    	public Page<CustomerDTO> search(
//    	        @RequestParam(required = false) String country,
//    	        @RequestParam(required = false) Boolean active,
//    	        @RequestParam(required = false) Integer minAge,
//    	        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)

    	// Fetch all customers and apply filters in-memory (better to do this in the database for large datasets)
    	// For simplicity, we're doing it in-memory here.
    	// In a real application, you'd want to implement this as a custom query in the repository.
//        List<Customer> result = repo.findAll().stream()
//                .filter(c -> country == null || c.getCountry().equalsIgnoreCase(country))
//                .filter(c -> active == null || c.isActive() == active)
//                .filter(c -> minAge == null || c.getAge() >= minAge)
//                .collect(Collectors.toList());
//
//        return result.stream().map(CustomerMapper::toDto).toList();
        
    	String sortField = sort[0];
        Sort.Direction direction =
                sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<CustomerDTO> pageResult = repo.search(country, active, minAge, pageable)
            .map(CustomerMapper::toDto);

        return new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
            );

    }
    
    @GetMapping("/search")
    public PageResponse<CustomerDTO> search(
    		 @RequestParam(required = false) String keyword, // search by firstName, lastName, email, phone ex: /api/v1/customers/search?keyword=John&country=USA&active=true&minAge=25
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String  createdBefore, // customer can specify date or both date and time, so we can parse it as LocalDateTime or LocalDate
            @RequestParam(required = false) String  createdAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sort[0]));

        LocalDateTime before = CustomerSpecs.parseDateOrDateTime(createdBefore, true);
        LocalDateTime after  = CustomerSpecs.parseDateOrDateTime(createdAfter, false);

        Specification<Customer> spec = Specification
                .where(CustomerSpecs.hasCountry(country))
                .and(CustomerSpecs.hasCity(city))
                .and(CustomerSpecs.isActive(active))
                .and(CustomerSpecs.ageRange(minAge, maxAge))
                .and(CustomerSpecs.createdBefore(before))
                .and(CustomerSpecs.createdAfter(after))
                .and(CustomerSpecs.keyword(keyword));


        Page<CustomerDTO> result = repo.findAll(spec, pageable)
                .map(CustomerMapper::toDto);

        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    
    // inteded to be used with a full-text search index (e.g. using Hibernate Search with Lucene or Elasticsearch)
    @GetMapping("/fullTextSearch")
    public PageResponse<CustomerDTO> fullTextSearch(
    		 @RequestParam(required = false) String keyword, // search for full text in firstName, lastName, email, ex: /api/v1/customers/search?keyword=John
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sort[0]));

//        LocalDateTime before = CustomerSpecs.parseDateOrDateTime(createdBefore, true);
//        LocalDateTime after  = CustomerSpecs.parseDateOrDateTime(createdAfter, false);

//        Specification<Customer> spec = Specification
//                .where(CustomerSpecs.hasCountry(country))
//                .and(CustomerSpecs.hasCity(city))
//                .and(CustomerSpecs.isActive(active))
//                .and(CustomerSpecs.ageRange(minAge, maxAge))
//                .and(CustomerSpecs.createdBefore(before))
//                .and(CustomerSpecs.createdAfter(after))
//                .and(CustomerSpecs.keyword(keyword));


        Page<CustomerDTO> result = repo.searchFullText(keyword, pageable)
                .map(CustomerMapper::toDto);

        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    
    
    // Cursor-based pagination example: /api/v1/customers/cursor?after=10&limit=5
    // This endpoint retrieves the next page of customers after the given cursor (customer ID) with a specified limit.
    // Cursor-based pagination is more efficient for large datasets and provides better performance compared to
    // offset-based pagination.
    
    @GetMapping("/cursor")
    public CursorPage<CustomerDTO> cursorPage(
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10") int limit) {

    	// Create a Pageable object with the specified limit and sorting by ID in ascending order
    	Pageable pageable = PageRequest.of(0, limit, Sort.by("id").ascending());
    	// Call the repository method to retrieve the next page of customers after the given cursor (ID)
        List<CustomerDTO> customers = repo.findNextPage(after, pageable).stream().map(CustomerMapper::toDto).toList();
        // Determine the next cursor (ID of the last customer in the current page) and whether there is a next page
        Long nextCursor = customers.isEmpty() ? null : customers.get(customers.size() - 1).getId();
        // If the number of customers retrieved is equal to the limit, it indicates that there may be more records to fetch,
        // so we set hasNext to true. Otherwise, if fewer records are retrieved, it means we've reached the end of the dataset, 
        //and hasNext is set to false.
        boolean hasNext = customers.size() == limit;


        return new CursorPage<CustomerDTO>(customers, nextCursor, 0L, hasNext, false);
    }
    
    @GetMapping("/cursorFilter")
    public CursorPage<CustomerDTO> cursorFilter(
    		 @RequestParam(required = false) String keyword, // search by firstName, lastName, email, phone ex: /api/v1/customers/search?keyword=John&country=USA&active=true&minAge=25
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String  createdBefore, // customer can specify date or both date and time, so we can parse it as LocalDateTime or LocalDate
            @RequestParam(required = false) String  createdAfter,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) Long after,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

//        Pageable pageable = PageRequest.of(page, size, Sort.by(sort[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sort[0]));
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by("id").ascending());

        LocalDateTime beforeDate = CustomerSpecs.parseDateOrDateTime(createdBefore, true);
        LocalDateTime afterDate  = CustomerSpecs.parseDateOrDateTime(createdAfter, false);

        Specification<Customer> spec = Specification
                .where(CustomerSpecs.hasCountry(country))
                .and(CustomerSpecs.hasCity(city))
                .and(CustomerSpecs.isActive(active))
                .and(CustomerSpecs.ageRange(minAge, maxAge))
                .and(CustomerSpecs.createdBefore(beforeDate))
                .and(CustomerSpecs.createdAfter(afterDate))
                .and(CustomerSpecs.keyword(keyword));
        
        if (after != null) {
            spec = spec.and(CustomerSpecs.afterId(after));
        }

        if (before != null) {
            spec = spec.and(CustomerSpecs.beforeId(before));
        }

        List<CustomerDTO> customers = repo.findAll(spec, pageable)
                .map(CustomerMapper::toDto).toList();
        
        Long nextCursor = customers.isEmpty() ? null : customers.get(customers.size() - 1).getId();
        Long prevCursor = customers.isEmpty() ? null : customers.get(0).getId();

        boolean hasNext = customers.size() == limit;
        boolean hasPrev = after != null || before != null;

        return new CursorPage<>(customers, nextCursor, prevCursor, hasNext, hasPrev);

   }

    
    @GetMapping("/age")  ///api/v1/customers/age?minAge=25
    public List<CustomerDTO> getByMinAge(@RequestParam int minAge) {
        List<Customer> customers = repo.findByAgeGreaterThanEqual(minAge);
        return customers.stream()
                        .map(CustomerMapper::toDto)
                        .collect(Collectors.toList());
    }
}
