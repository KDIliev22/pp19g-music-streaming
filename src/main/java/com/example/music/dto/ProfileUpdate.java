package com.example.music.dto;

import jakarta.validation.constraints.*;

public record ProfileUpdate(
        @NotBlank(message = "Името е задължително") @Size(max = 100, message = "Името е твърде дълго") String fullName,
        @Email(message = "Невалиден email") String email,
        @Pattern(regexp = "(|[+0-9 ()-]{6,20})", message = "Невалиден телефон") String phone) {}
