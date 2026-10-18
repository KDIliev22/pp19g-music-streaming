package com.example.music.dto;

public record RecommendationDto(TrackDto track, double score, String reason) {}
