package com.example.music.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user") // "user" is reserved in Postgres
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String username;
    @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
    private String fullName;
    @Column(unique = true) private String email;
    private String phone;
    @Column(updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    /** Soft delete: deactivated users stay in the database but can no longer log in. */
    @Column(nullable = false, columnDefinition = "boolean default true") private boolean active = true;

    protected AppUser() {}
    public AppUser(String username, String password, String fullName, String email, String phone, Role role) {
        this.username = username; this.password = password; this.fullName = fullName;
        this.email = email; this.phone = phone; this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRole(Role role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setActive(boolean active) { this.active = active; }
}
