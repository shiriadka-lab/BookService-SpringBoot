package com.learn.bookService.persistence.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.bookService.persistence.model.Users;

// This repository is used to interact with the database for user-related operations
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);
}
