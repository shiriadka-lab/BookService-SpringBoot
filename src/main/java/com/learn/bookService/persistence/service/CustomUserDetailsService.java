package com.learn.bookService.persistence.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.learn.bookService.persistence.model.Users;
import com.learn.bookService.persistence.repo.UserRepository;

// This service is used by Spring Security to load user details during authentication
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // This method is called by Spring Security to load user details by username
    //  It retrieves the user from the database using the UserRepository
    // If the user is not found, it throws a UsernameNotFoundException
    @Override
    public UserDetails loadUserByUsername(String username) {

    	// Retrieve the user from the database using the UserRepository
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Convert the user's roles to a set of GrantedAuthority objects
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        return new User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}