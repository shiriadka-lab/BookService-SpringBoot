package com.learn.bookService.dto;

import java.util.Set;

public class UsersDTO {
    private Long id;
    private String username;
    private Set<RoleDTO> roles;

    public UsersDTO() {}

    public UsersDTO(Long id, String username, Set<RoleDTO> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }
}
