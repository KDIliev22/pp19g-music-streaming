package com.example.music.dto;

import jakarta.validation.constraints.*;

public record NewPlaylist(@NotBlank(message = "Name your playlist") @Size(max = 60, message = "Name is too long") String name) {}
