package com.example.music.dto;

import com.example.music.model.Role;
import jakarta.validation.constraints.*;

public record CreateUser(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,32}", message = "Потребителското име е 3–32 символа: букви, цифри, . _ -") String username,
        @NotNull @Size(min = 8, max = 100, message = "Паролата трябва да е поне 8 символа") String password,
        @NotBlank(message = "Името е задължително") @Size(max = 100, message = "Името е твърде дълго") String fullName,
        @Email(message = "Невалиден email") String email,
        @Pattern(regexp = "(|[+0-9 ()-]{6,20})", message = "Невалиден телефон") String phone,
        @NotNull(message = "Изберете роля") Role role) {}
