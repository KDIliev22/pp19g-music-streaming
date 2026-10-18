package com.example.music.service;

import com.example.music.dto.EventLine;
import com.example.music.dto.MonthLine;
import com.example.music.dto.Report;
import com.example.music.repository.EventRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Builds the "Отчети" report. A report always covers WHOLE calendar months: it ends with the month
 * of the chosen date and goes back 1, 6 or 12 months, never further than 12 months from today.
 */
@Service
public class ReportService {


    private final EventRepository eventRepository;

    public ReportService(EventRepository eventRepository) { this.eventRepository = eventRepository; }

    public Report build(LocalDate date, int months) {
        if (months != 1 && months != 6 && months != 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "months must be 1, 6 or 12");
        YearMonth earliest = YearMonth.now().minusMonths(11);
        YearMonth end = YearMonth.from(date);
        if (end.isAfter(YearMonth.now()) || end.isBefore(earliest))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must be within the last 12 months");
        YearMonth start = end.minusMonths(months - 1L);
        if (start.isBefore(earliest)) start = earliest;

        LocalDate from = start.atDay(1), to = end.atEndOfMonth();
        List<EventLine> lines = eventRepository.findByEventDateBetweenOrderByEventDateAsc(from, to).stream()
                .map(e -> new EventLine(e.getId(), e.getTitle(), e.getEventDate(), e.getTicketPrice(), e.getTicketsSold(),
                        e.getTicketPrice().multiply(BigDecimal.valueOf(e.getTicketsSold()))))
                .toList();

        List<MonthLine> monthly = new ArrayList<>();
        for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
            YearMonth current = m;
            List<EventLine> inMonth = lines.stream().filter(l -> YearMonth.from(l.date()).equals(current)).toList();
            monthly.add(new MonthLine(current.toString(), inMonth.size(), total(inMonth)));
        }
        return new Report(from, to, months, lines.size(), total(lines), monthly, lines);
    }

    private static BigDecimal total(List<EventLine> lines) {
        return lines.stream().map(EventLine::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
