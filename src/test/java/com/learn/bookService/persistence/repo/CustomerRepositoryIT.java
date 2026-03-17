package com.learn.bookService.persistence.repo;

import static org.assertj.core.api.Assertions.assertThat;

import com.learn.bookService.persistence.model.Customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CustomerRepositoryIT {

    @Autowired
    CustomerRepository repo;

    @Test
    void saveAndFind() {
        Customer c = new Customer();
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setEmail("john.doe@example.com");

        Customer saved = repo.save(c);
        assertThat(saved.getId()).isNotNull();

        Customer found = repo.findById(saved.getId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("john.doe@example.com");
    }
}
