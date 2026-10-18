package com.example.music.dto;

import com.example.music.model.AppUser;
import com.example.music.model.Role;
public record UserView(Long id, String username, String fullName, String email, String phone, Role role, boolean active) {
    public static UserView of(AppUser u) {
        return new UserView(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(), u.isActive());
    }
}
