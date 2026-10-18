package com.example.music.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Report(LocalDate from, LocalDate to, int months, int eventCount, BigDecimal totalRevenue, List<MonthLine> monthly, List<EventLine> events) {}
