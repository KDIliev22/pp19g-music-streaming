package com.example.music.dto;

import java.time.LocalDateTime;

public record Registration(String type, Long id, String name, String email, LocalDateTime createdAt) {}
