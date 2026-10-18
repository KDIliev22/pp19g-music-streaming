package com.example.music.dto;

import com.example.music.model.Role;
import jakarta.validation.constraints.NotNull;

public record RoleChange(@NotNull(message = "Изберете роля") Role role) {}
