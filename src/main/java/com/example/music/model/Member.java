package com.example.music.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "club_member")
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String fullName;
    private String email;
    private String phone;
    @Column(updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false, columnDefinition = "boolean default true") private boolean active = true;

    protected Member() {}
    public Member(String fullName, String email, String phone, boolean active, LocalDateTime createdAt) {
        this.fullName = fullName; this.email = email; this.phone = phone; this.active = active; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
