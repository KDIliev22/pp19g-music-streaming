package com.example.music.dto;

import java.util.List;
import java.util.Map;

public record RegistrationReport(String period, Map<String, Long> counts, long userCount, long memberCount,
                                 List<Registration> items, List<MonthCount> perMonth) {}
