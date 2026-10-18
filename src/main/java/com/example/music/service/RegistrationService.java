package com.example.music.service;

import com.example.music.dto.MonthCount;
import com.example.music.dto.Registration;
import com.example.music.dto.RegistrationReport;
import com.example.music.repository.MemberRepository;
import com.example.music.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Newly registered users and members: today / last week / last month / last year, plus a 12-month trend. */
@Service
public class RegistrationService {

    public enum Period {
        TODAY, WEEK, MONTH, YEAR;

        LocalDateTime since(LocalDateTime now) {
            return switch (this) {
                case TODAY -> now.toLocalDate().atStartOfDay();
                case WEEK -> now.minusDays(7);
                case MONTH -> now.minusMonths(1);
                case YEAR -> now.minusYears(1);
            };
        }
    }

    private final UserRepository users;
    private final MemberRepository members;

    public RegistrationService(UserRepository users, MemberRepository members) { this.users = users; this.members = members; }

    public RegistrationReport report(Period period) {
        LocalDateTime now = LocalDateTime.now();
        List<Registration> all = new ArrayList<>();
        users.findAll().stream().filter(u -> u.getCreatedAt() != null).forEach(u -> all.add(new Registration("USER", u.getId(),
                u.getFullName() != null ? u.getFullName() : u.getUsername(), u.getEmail(), u.getCreatedAt())));
        members.findAll().stream().filter(m -> m.getCreatedAt() != null).forEach(m -> all.add(new Registration("MEMBER", m.getId(),
                m.getFullName(), m.getEmail(), m.getCreatedAt())));

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Period p : Period.values())
            counts.put(p.name(), all.stream().filter(r -> !r.createdAt().isBefore(p.since(now))).count());

        LocalDateTime since = period.since(now);
        List<Registration> items = all.stream().filter(r -> !r.createdAt().isBefore(since))
                .sorted(Comparator.comparing(Registration::createdAt).reversed()).toList();
        long userCount = items.stream().filter(r -> r.type().equals("USER")).count();

        List<MonthCount> perMonth = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth m = YearMonth.from(now).minusMonths(i);
            perMonth.add(new MonthCount(m.toString(), all.stream().filter(r -> YearMonth.from(r.createdAt()).equals(m)).count()));
        }
        return new RegistrationReport(period.name(), counts, userCount, items.size() - userCount, items, perMonth);
    }
}
