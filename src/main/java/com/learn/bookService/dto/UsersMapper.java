package com.learn.bookService.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.learn.bookService.persistence.model.Role;
import com.learn.bookService.persistence.model.Users;

public class UsersMapper {

    public static RoleDTO toRoleDTO(Role r) {
        if (r == null) return null;
        return new RoleDTO(r.getId(), r.getName());
    }

    public static UsersDTO toUsersDTO(Users u) {
        if (u == null) return null;
        Set<RoleDTO> roles = null;
        if (u.getRoles() != null) {
            roles = u.getRoles().stream().map(UsersMapper::toRoleDTO).collect(Collectors.toSet());
        }
        return new UsersDTO(u.getId(), u.getUsername(), roles);
    }
}
