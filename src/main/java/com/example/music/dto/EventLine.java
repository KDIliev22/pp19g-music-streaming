package com.example.music.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventLine(Long id, String title, LocalDate date, BigDecimal ticketPrice, int ticketsSold, BigDecimal revenue) {}
