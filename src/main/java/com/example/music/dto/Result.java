package com.example.music.dto;

import java.util.List;

public record Result(List<Hit> items, int page, int size, long totalItems, int totalPages) {}
