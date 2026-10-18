package com.example.music.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderPage(List<OrderDto> items, int page, int size, long totalItems, int totalPages, BigDecimal totalAmount) {}
