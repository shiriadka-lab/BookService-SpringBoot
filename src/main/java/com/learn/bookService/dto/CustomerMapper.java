package com.learn.bookService.dto;

import com.learn.bookService.persistence.model.Customer;

public class CustomerMapper {

    public static CustomerDTO toDto(Customer c) {
        if (c == null) return null;
        CustomerDTO d = new CustomerDTO();
        d.setId(c.getId());
        d.setFirstName(c.getFirstName());
        d.setLastName(c.getLastName());
        d.setEmail(c.getEmail());
        d.setPhone(c.getPhone());
        d.setAge(c.getAge());
        d.setCity(c.getCity());
        d.setCountry(c.getCountry());
        d.setActive(c.isActive());
        d.setStatus(c.getStatus());
        d.setCreatedAt(c.getCreatedAt());
        d.setUpdatedAt(c.getUpdatedAt());
        return d;
    }

    public static Customer fromDto(CustomerDTO d) {
        if (d == null) return null;
        Customer c = new Customer();
        c.setFirstName(d.getFirstName());
        c.setLastName(d.getLastName());
        c.setEmail(d.getEmail());
        c.setPhone(d.getPhone());
        c.setAge(d.getAge());
        c.setCity(d.getCity());
        c.setCountry(d.getCountry());
        c.setActive(d.isActive());
        c.setStatus(d.getStatus());
        return c;
    }
}
