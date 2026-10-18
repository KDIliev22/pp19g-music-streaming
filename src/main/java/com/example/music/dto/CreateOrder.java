package com.example.music.dto;

import jakarta.validation.constraints.*;

public record CreateOrder(
        @NotNull(message = "Изберете събитие") Long eventId,
        @NotNull(message = "Изберете член") Long memberId,
        @Min(value = 1, message = "Минимум 1 билет") @Max(value = 20, message = "Максимум 20 билета") int quantity) {}
