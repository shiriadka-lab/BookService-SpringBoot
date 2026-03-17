package com.learn.bookService.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.bookService.client.PricingClient;
import com.learn.bookService.dto.CustomerDTO;
import com.learn.bookService.persistence.model.Customer;
import com.learn.bookService.persistence.repo.CustomerRepository;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository repo;
    
    @MockBean
    private PricingClient pricingClient;  // Mock it so URL is never needed

    @Autowired
    private ObjectMapper mapper;

    private static Customer customer(long id, String firstName, String lastName, String email) {
        Customer c = new Customer(firstName, lastName, email);
        try {
            Field idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(c, Long.valueOf(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        c.setPhone("+123");
        c.setAge(30);
        c.setCity("Toronto");
        c.setCountry("Canada");
        c.setActive(true);
        c.setStatus("ACTIVE");
//        c.setCreatedAt(LocalDateTime.now());
//        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private static Customer customerWithId(long id) {
        return customer(id, "Jane", "Doe", "jane@example.com");
    }

    @Nested
    @DisplayName("GET /api/v1/customers (list)")
    class ListTests {

        @Test
        @DisplayName("returns paginated list with default params")
        void listReturnsPaginatedWithDefaults() throws Exception {
            Customer c = customerWithId(1L);
            Pageable defaultPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
            Page<Customer> page = new PageImpl<>(List.of(c), defaultPageable, 1);
            when(repo.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].firstName").value("Jane"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                    .andExpect(jsonPath("$.content[0].email").value("jane@example.com"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("accepts custom page, size and sort")
        void listAcceptsCustomPaginationAndSort() throws Exception {
            Page<Customer> page = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
            when(repo.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sort", "firstName", "desc"))
                    .andExpect(status().isOk());
            verify(repo).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("returns empty content when no customers")
        void listReturnsEmptyWhenNoCustomers() throws Exception {
            Page<Customer> page = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
            when(repo.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("returns 200 and customer when found")
        void getReturnsOkWhenFound() throws Exception {
            Customer c = customerWithId(1L);
            when(repo.findById(1L)).thenReturn(Optional.of(c));

            mockMvc.perform(get("/api/v1/customers/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.email").value("jane@example.com"));
        }

        @Test
        @DisplayName("returns 404 when not found")
        void getReturnsNotFoundWhenMissing() throws Exception {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/customers/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers (create)")
    class CreateTests {

        @Test
        @DisplayName("returns 200 and created customer when email is new")
        void createReturnsOkWhenEmailNew() throws Exception {
            CustomerDTO dto = new CustomerDTO();
            dto.setFirstName("New");
            dto.setLastName("User");
            dto.setEmail("new@example.com");
            dto.setPhone("+456");
            dto.setAge(25);
            dto.setCity("Vancouver");
            dto.setCountry("Canada");
            dto.setActive(true);
            dto.setStatus("ACTIVE");

            when(repo.findByEmail("new@example.com")).thenReturn(Optional.empty());
            Customer saved = customerWithId(1L);
            saved.setFirstName("New");
            saved.setLastName("User");
            saved.setEmail("new@example.com");
            when(repo.save(any(Customer.class))).thenReturn(saved);

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.email").value("new@example.com"));
        }

        @Test
        @DisplayName("returns 409 when email already exists")
        void createConflictWhenEmailExists() throws Exception {
            CustomerDTO dto = new CustomerDTO();
            dto.setFirstName("Test");
            dto.setLastName("User");
            dto.setEmail("x@y.com");

            when(repo.findByEmail("x@y.com")).thenReturn(Optional.of(new Customer()));

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/{id}")
    class UpdateTests {

        @Test
        @DisplayName("returns 200 and updated customer when found")
        void updateReturnsOkWhenFound() throws Exception {
            Customer existing = customerWithId(1L);
            when(repo.findById(1L)).thenReturn(Optional.of(existing));

            CustomerDTO dto = new CustomerDTO();
            dto.setFirstName("Updated");
            dto.setLastName("Name");
            dto.setPhone("+999");
            dto.setAge(35);
            dto.setCity("Montreal");
            dto.setCountry("Canada");
            dto.setActive(false);
            dto.setStatus("INACTIVE");

            Customer saved = customerWithId(1L);
            saved.setFirstName("Updated");
            saved.setLastName("Name");
            when(repo.save(any(Customer.class))).thenReturn(saved);

            mockMvc.perform(put("/api/v1/customers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"));
        }

        @Test
        @DisplayName("returns 404 when customer not found")
        void updateReturnsNotFoundWhenMissing() throws Exception {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            CustomerDTO dto = new CustomerDTO();
            dto.setFirstName("Any");
            dto.setLastName("User");
            dto.setStatus("ACTIVE");

            mockMvc.perform(put("/api/v1/customers/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{id}")
    class DeleteTests {

        @Test
        @DisplayName("returns 204 when customer exists")
        void deleteReturnsNoContentWhenFound() throws Exception {
            Customer c = customerWithId(1L);
            when(repo.findById(1L)).thenReturn(Optional.of(c));

            mockMvc.perform(delete("/api/v1/customers/1"))
                    .andExpect(status().isNoContent());
            verify(repo).delete(c);
        }

        @Test
        @DisplayName("returns 404 when customer not found")
        void deleteReturnsNotFoundWhenMissing() throws Exception {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/v1/customers/999"))
                    .andExpect(status().isNotFound());
            verify(repo).findById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/customerByFilter")
    class CustomerByFilterTests {

        @Test
        @DisplayName("returns paginated result with optional filters")
        void searchWithFiltersReturnsPage() throws Exception {
            Customer c = customerWithId(1L);
            Page<Customer> page = new PageImpl<>(List.of(c), Pageable.unpaged(), 1);
            when(repo.search(eq("Canada"), eq(true), eq(25), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers/customerByFilter")
                            .param("country", "Canada")
                            .param("active", "true")
                            .param("minAge", "25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].country").value("Canada"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("works with no filters (all null)")
        void searchWithNoFilters() throws Exception {
            Page<Customer> page = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
            when(repo.search(eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers/customerByFilter"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/search (specification search)")
    class SearchSpecTests {

        @Test
        @DisplayName("returns paginated result with specification filters")
        void searchWithSpecReturnsPage() throws Exception {
            Customer c = customerWithId(1L);
            Page<Customer> page = new PageImpl<>(List.of(c), Pageable.unpaged(), 1);
            when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("country", "Canada")
                            .param("city", "Toronto")
                            .param("active", "true")
                            .param("minAge", "20")
                            .param("maxAge", "40"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1));
        }

        @Test
        @DisplayName("accepts date filters createdBefore and createdAfter")
        void searchWithDateFilters() throws Exception {
            Page<Customer> page = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
            when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("createdAfter", "2024-01-01")
                            .param("createdBefore", "2025-12-31"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/age")
    class GetByMinAgeTests {

        @Test
        @DisplayName("returns list of customers with age >= minAge")
        void getByMinAgeReturnsList() throws Exception {
            Customer c = customerWithId(1L);
            when(repo.findByAgeGreaterThanEqual(25)).thenReturn(List.of(c));

            mockMvc.perform(get("/api/v1/customers/age").param("minAge", "25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].age").value(30));
        }

        @Test
        @DisplayName("returns empty list when no customers match")
        void getByMinAgeReturnsEmptyWhenNoneMatch() throws Exception {
            when(repo.findByAgeGreaterThanEqual(100)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/customers/age").param("minAge", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
